package com.vinayms.razorpayclone.webhook;

import com.vinayms.razorpayclone.merchant.config.WebhookSecretEncryption;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {
    private static final int MAX_TEXT_LENGTH = 1000;

    private final WebhookDeliveryStateService stateService;
    private final HttpClient webhookHttpClient;
    private final ExecutorService webhookVirtualThreadExecutor;
    private final WebhookSecretEncryption secretEncryption;

    @Scheduled(fixedDelayString = "${webhook.delivery.poll-interval-ms:1000}")
    public void dispatchDue() {
        for (UUID id : stateService.claimDueEvents()) webhookVirtualThreadExecutor.submit(() -> deliver(id));
    }

    private void deliver(UUID id) {
        WebhookDeliveryStateService.Delivery delivery = stateService.load(id);
        if (delivery == null) return;
        int statusCode = 0;
        String responseBody = null;
        String error = null;
        String signature = null;
        try {
            String body = delivery.payloadJson();
            signature = sign(body, secretEncryption.decrypt(delivery.encryptedSecret()));
            HttpRequest request = HttpRequest.newBuilder(URI.create(delivery.targetUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Signature", "sha256=" + signature)
                    .header("X-Webhook-Event", delivery.eventType())
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = webhookHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            statusCode = response.statusCode();
            responseBody = truncate(response.body());
            if (statusCode < 200 || statusCode >= 300) error = "Webhook returned HTTP " + statusCode;
        } catch (Exception ex) {
            error = truncate(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            responseBody = error;
        }
        stateService.completeAttempt(id, statusCode, responseBody, signature, error);
    }

    private String sign(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() <= MAX_TEXT_LENGTH ? value : value.substring(0, MAX_TEXT_LENGTH);
    }

}

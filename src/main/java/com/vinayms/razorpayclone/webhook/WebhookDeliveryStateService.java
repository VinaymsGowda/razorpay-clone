package com.vinayms.razorpayclone.webhook;

import com.vinayms.razorpayclone.common.enums.WebhookEventStatus;
import com.vinayms.razorpayclone.operations.entity.DlqEvent;
import com.vinayms.razorpayclone.operations.entity.WebhookEvent;
import com.vinayms.razorpayclone.operations.repository.DlqEventRepository;
import com.vinayms.razorpayclone.operations.repository.WebhookEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookDeliveryStateService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final Duration CLAIM_TIMEOUT = Duration.ofMinutes(2);
    private final WebhookEventRepository eventRepository;
    private final DlqEventRepository dlqRepository;

    @Transactional
    public List<UUID> claimDueEvents() {
        LocalDateTime now = LocalDateTime.now();
        for (WebhookEvent stale : eventRepository.findStaleClaims(WebhookEventStatus.PROCESSING, now.minus(CLAIM_TIMEOUT))) {
            stale.setStatus(WebhookEventStatus.PENDING);
            stale.setNextRetryAt(now);
        }
        List<WebhookEvent> due = eventRepository.findDue(WebhookEventStatus.PENDING, now, PageRequest.ofSize(50));
        for (WebhookEvent event : due) {
            event.setStatus(WebhookEventStatus.PROCESSING);
            event.setLastAttemptAt(now);
        }
        return due.stream().map(WebhookEvent::getId).toList();
    }

    @Transactional
    public Delivery load(UUID id) {
        return eventRepository.findById(id).filter(event -> event.getStatus() == WebhookEventStatus.PROCESSING)
                .map(event -> new Delivery(event.getTargetUrl(), event.getPayloadJson(), event.getEncryptedSecret(), event.getEventType()))
                .orElse(null);
    }

    @Transactional
    public void completeAttempt(UUID id, int responseCode, String responseBody, String signature, String error) {
        WebhookEvent event = eventRepository.findById(id).orElse(null);
        if (event == null || event.getStatus() != WebhookEventStatus.PROCESSING) return;
        int attempts = event.getAttempts() + 1;
        LocalDateTime now = LocalDateTime.now();
        event.setAttempts(attempts);
        event.setLastAttemptAt(now);
        event.setLastResponseCode(responseCode == 0 ? null : responseCode);
        event.setLastResponseBody(truncate(responseBody));
        event.setSignature(signature == null ? "" : signature);
        if (error == null) {
            event.setStatus(WebhookEventStatus.DELIVERED);
            event.setDeliveredAt(now);
            event.setNextRetryAt(null);
            return;
        }
        if (attempts >= MAX_ATTEMPTS) {
            event.setStatus(WebhookEventStatus.DEAD);
            DlqEvent dlq = new DlqEvent();
            dlq.setMerchantId(event.getMerchantId());
            dlq.setWebhookEvent(event);
            dlq.setFinalError(truncate(error));
            dlq.setPayload(event.getPayload());
            dlq.setMovedAt(now);
            dlqRepository.save(dlq);
        } else {
            event.setStatus(WebhookEventStatus.PENDING);
            event.setNextRetryAt(now.plusSeconds(1L << (attempts - 1)));
        }
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() <= MAX_TEXT_LENGTH ? value : value.substring(0, MAX_TEXT_LENGTH);
    }

    public record Delivery(String targetUrl, String payloadJson, String encryptedSecret, String eventType) { }
}

package com.vinayms.razorpayclone.webhook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vinayms.razorpayclone.common.enums.WebhookEventStatus;
import com.vinayms.razorpayclone.merchant.config.WebhookSecretEncryption;
import com.vinayms.razorpayclone.merchant.entity.MerchantWebhookConfig;
import com.vinayms.razorpayclone.merchant.repository.MerchantWebhookConfigRepository;
import com.vinayms.razorpayclone.operations.entity.WebhookEvent;
import com.vinayms.razorpayclone.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventConsumer {
    private final ObjectMapper objectMapper;
    private final MerchantWebhookConfigRepository configRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookSecretEncryption secretEncryption;

    @KafkaListener(topics = {"${kafka.topics.payment}", "${kafka.topics.order}", "${kafka.topics.refund}", "${kafka.topics.settlement}"})
    @Transactional
    public void consume(ConsumerRecord<String, String> record) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(record.value(), new TypeReference<>() {});
        Object merchantValue = payload.get("merchantId");
        if (merchantValue == null) throw new IllegalArgumentException("Kafka webhook event is missing merchantId");
        UUID merchantId = UUID.fromString(merchantValue.toString());
        String eventType = header(record, "event-type");
        String sourceId = headerOptional(record, "event-id");
        if (sourceId == null || sourceId.isBlank()) sourceId = record.topic() + ":" + record.partition() + ":" + record.offset();

        for (MerchantWebhookConfig config : configRepository.findByMerchant_IdAndIsActiveTrue(merchantId)) {
            log.info("Config data: {}", config.getId());
            if (!accepts(config.getEventType(), eventType)) continue;
            if (eventRepository.existsBySourceEventIdAndWebhookConfigId(sourceId, config.getId())) continue;
            WebhookEvent event = new WebhookEvent();
            event.setMerchantId(merchantId);
            event.setSourceEventId(sourceId);
            event.setWebhookConfigId(config.getId());
            event.setEventType(eventType);
            event.setPayload(payload);
            String payloadJson = objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                    .writeValueAsString(payload);
            event.setPayloadJson(payloadJson);
            event.setTargetUrl(config.getWebhookUrl());
            event.setEncryptedSecret(config.getWebhookSecret());
            event.setSignature(sign(payloadJson, config.getWebhookSecret()));
            event.setStatus(WebhookEventStatus.PENDING);
            event.setAttempts(0);
            eventRepository.save(event);
        }
    }

    private String header(ConsumerRecord<String, String> record, String name) {
        String value = headerOptional(record, name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Kafka event is missing " + name + " header");
        return value;
    }

    private String headerOptional(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

    private boolean accepts(String configuredTypes, String eventType) {
        if (configuredTypes == null) return false;
        return Arrays.stream(configuredTypes.split(","))
                .map(String::trim)
                .map(value -> value.equals("ORDER_CANCELLED") ? "ORDER_CANCELED" : value)
                .anyMatch(eventType::equals);
    }

    private String sign(String payloadJson, String encryptedSecret) throws Exception {
        String secret = secretEncryption.decrypt(encryptedSecret);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payloadJson.getBytes(StandardCharsets.UTF_8)));
    }
}

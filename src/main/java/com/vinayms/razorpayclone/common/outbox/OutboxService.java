package com.vinayms.razorpayclone.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinayms.razorpayclone.common.config.KafkaTopicProperties;
import com.vinayms.razorpayclone.common.entity.OutboxEntity;
import com.vinayms.razorpayclone.common.enums.EventAggregateType;
import com.vinayms.razorpayclone.common.enums.OutboxStatus;
import com.vinayms.razorpayclone.common.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Write API for the outbox. Must be called inside the business transaction so the event is
 * persisted atomically with the state change.
 */
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTopicProperties topicProperties;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEntity record(EventAggregateType aggregateType, UUID aggregateId, String eventType, Object payload) {
        String json;
        try {
            json = payload instanceof String s ? s : objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize outbox payload for " + eventType, e);
        }

        OutboxEntity event = new OutboxEntity();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(json);
        event.setTopic(topicProperties.getTopic(aggregateType));
        event.setStatus(OutboxStatus.PENDING);
        event.setAttempts(0);
        event.setNextAttemptAt(LocalDateTime.now());
        return outboxRepository.save(event);
    }
}

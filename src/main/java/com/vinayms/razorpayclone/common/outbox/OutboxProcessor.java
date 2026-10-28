package com.vinayms.razorpayclone.common.outbox;

import com.vinayms.razorpayclone.common.entity.OutboxEntity;
import com.vinayms.razorpayclone.common.enums.OutboxStatus;
import com.vinayms.razorpayclone.common.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes one batch of due outbox events. Kept separate from {@link OutboxPoller} so the
 * transactional proxy is applied.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    public static final int MAX_ATTEMPTS = 5;

    /** Backoff applied after the 1st, 2nd, ... failed attempt. */
    private static final Duration[] BACKOFF = {
            Duration.ofSeconds(2),
            Duration.ofSeconds(10),
            Duration.ofSeconds(30),
            Duration.ofMinutes(2),
            Duration.ofMinutes(5)
    };

    /** Delay used when the broker itself is unreachable (attempts are not consumed). */
    private static final Duration BROKER_DOWN_DELAY = Duration.ofSeconds(30);

    private static final int MAX_ERROR_LENGTH = 1000;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Value("${outbox.poller.send-timeout-ms:10000}")
    private long sendTimeoutMs;

    private record InFlight(OutboxEntity event, CompletableFuture<SendResult<String, String>> future) {
    }

    @Transactional
    public void processBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxEntity> events = outboxRepository.findDueEvents(OutboxStatus.PENDING, now, PageRequest.ofSize(batchSize));
        if (events.isEmpty()) {
            return;
        }
        log.debug("Publishing {} outbox events", events.size());

        List<InFlight> inFlight = new ArrayList<>(events.size());
        for (OutboxEntity event : events) {
            try {
                inFlight.add(new InFlight(event, send(event)));
            } catch (Exception ex) {
                handleFailure(event, ex);
            }
        }

        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(sendTimeoutMs);
        for (InFlight item : inFlight) {
            OutboxEntity event = item.event();
            try {
                long remaining = Math.max(1, deadline - System.nanoTime());
                item.future().get(remaining, TimeUnit.NANOSECONDS);
                markPublished(event);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                handleFailure(event, ie);
            } catch (ExecutionException ee) {
                handleFailure(event, ee.getCause() != null ? ee.getCause() : ee);
            } catch (Exception ex) {
                handleFailure(event, ex);
            }
        }
    }

    private CompletableFuture<SendResult<String, String>> send(OutboxEntity event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                event.getTopic(), null, event.getAggregateId().toString(), event.getPayload());
        record.headers()
                .add("event-id", event.getId().toString().getBytes(StandardCharsets.UTF_8))
                .add("event-type", event.getEventType().getBytes(StandardCharsets.UTF_8))
                .add("aggregate-type", event.getAggregateType().name().getBytes(StandardCharsets.UTF_8));
        return kafkaTemplate.send(record);
    }

    private void markPublished(OutboxEntity event) {
        event.setStatus(OutboxStatus.COMPLETED);
        event.setPublishedAt(LocalDateTime.now());
        event.setErrorMessage(null);
    }

    private void handleFailure(OutboxEntity event, Throwable error) {
        String message = truncate(error.getClass().getSimpleName() + ": " + error.getMessage());
        event.setErrorMessage(message);

        if (isBrokerUnavailable(error)) {
            // Broker-wide problem: do not burn attempts, just back off.
            event.setNextAttemptAt(LocalDateTime.now().plus(BROKER_DOWN_DELAY));
            log.warn("Kafka unavailable while publishing outbox event {}: {}", event.getId(), message);
            return;
        }

        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);

        if (attempts >= MAX_ATTEMPTS) {
            event.setStatus(OutboxStatus.FAILED);
            log.error("Outbox event {} marked FAILED after {} attempts: {}", event.getId(), attempts, message);
        } else {
            event.setNextAttemptAt(LocalDateTime.now().plus(BACKOFF[Math.min(attempts - 1, BACKOFF.length - 1)]));
            log.warn("Outbox event {} failed (attempt {}/{}): {}", event.getId(), attempts, MAX_ATTEMPTS, message);
        }
    }

    private boolean isBrokerUnavailable(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof TimeoutException
                    || t instanceof org.apache.kafka.common.errors.TimeoutException
                    || t instanceof org.apache.kafka.common.errors.NetworkException
                    || t instanceof org.apache.kafka.common.errors.DisconnectException) {
                return true;
            }
            if (t instanceof RetriableException) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
    }
}

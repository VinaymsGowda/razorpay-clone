package com.vinayms.razorpayclone.common.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelayString = "${outbox.poller.delay-ms:2000}")
    public void poll() {
        try {
            outboxProcessor.processBatch();
        } catch (Exception ex) {
            log.error("Outbox poll failed", ex);
        }
    }
}

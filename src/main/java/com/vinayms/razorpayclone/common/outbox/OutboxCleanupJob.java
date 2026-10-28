package com.vinayms.razorpayclone.common.outbox;

import com.vinayms.razorpayclone.common.enums.OutboxStatus;
import com.vinayms.razorpayclone.common.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Deletes COMPLETED outbox events older than the retention period (20 days by default).
 * Runs daily so the retention window stays accurate across restarts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxCleanupJob {

    private final OutboxRepository outboxRepository;

    @Value("${outbox.cleanup.retention-days:20}")
    private long retentionDays;

    @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = outboxRepository.deleteByStatusAndPublishedAtBefore(OutboxStatus.COMPLETED, cutoff);
        log.info("Outbox cleanup removed {} COMPLETED events older than {}", deleted, cutoff);
    }
}

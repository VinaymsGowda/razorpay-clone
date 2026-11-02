package com.vinayms.razorpayclone.operations.repository;

import com.vinayms.razorpayclone.common.enums.WebhookEventStatus;
import com.vinayms.razorpayclone.operations.entity.WebhookEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    boolean existsBySourceEventIdAndWebhookConfigId(String sourceEventId, UUID webhookConfigId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select e from WebhookEvent e where e.status = :status and (e.nextRetryAt is null or e.nextRetryAt <= :now) order by e.nextRetryAt asc, e.id asc")
    List<WebhookEvent> findDue(@Param("status") WebhookEventStatus status, @Param("now") LocalDateTime now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select e from WebhookEvent e where e.status = :status and e.lastAttemptAt < :before")
    List<WebhookEvent> findStaleClaims(@Param("status") WebhookEventStatus status, @Param("before") LocalDateTime before);
}

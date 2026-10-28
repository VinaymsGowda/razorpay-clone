package com.vinayms.razorpayclone.common.repository;

import com.vinayms.razorpayclone.common.entity.OutboxEntity;
import com.vinayms.razorpayclone.common.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Returns due PENDING events, oldest first, locking them with SKIP LOCKED so several
     * app instances never publish the same row.
     *
     * Only the head-of-line event per aggregate is eligible (no earlier PENDING event for the same
     * aggregate), which preserves per-aggregate ordering even across retries and instances.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            SELECT o FROM OutboxEntity o
            WHERE o.status = :status
              AND o.nextAttemptAt <= :now
              AND NOT EXISTS (
                    SELECT 1 FROM OutboxEntity p
                    WHERE p.aggregateId = o.aggregateId
                      AND p.status = :status
                      AND p.id <> o.id
                      AND (p.createdAt < o.createdAt
                           OR (p.createdAt = o.createdAt AND p.id < o.id))
              )
            ORDER BY o.createdAt ASC, o.id ASC
            """)
    List<OutboxEntity> findDueEvents(@Param("status") OutboxStatus status,
                                     @Param("now") LocalDateTime now,
                                     Pageable pageable);

    @Modifying
    @Query("DELETE FROM OutboxEntity o WHERE o.status = :status AND o.publishedAt < :before")
    int deleteByStatusAndPublishedAtBefore(@Param("status") OutboxStatus status,
                                           @Param("before") LocalDateTime before);
}

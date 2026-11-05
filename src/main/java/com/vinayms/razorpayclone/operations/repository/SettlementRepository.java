package com.vinayms.razorpayclone.operations.repository;

import com.vinayms.razorpayclone.common.enums.SettlementStatus;
import com.vinayms.razorpayclone.operations.entity.Settlement;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            select s from Settlement s
            where s.status = :status
              and (s.nextRetryAt is null or s.nextRetryAt <= :now)
            order by s.createdAt
            """)
    List<Settlement> findDuePending(
            @Param("status") SettlementStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            select s from Settlement s
            where s.id = :id
              and s.status = :status
              and (s.nextRetryAt is null or s.nextRetryAt <= :now)
            """)
    Optional<Settlement> lockDueById(
            @Param("id") UUID id,
            @Param("status") SettlementStatus status,
            @Param("now") LocalDateTime now);
}

package com.vinayms.razorpayclone.payment.repository;

import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrder_Id(UUID orderId);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    Optional<Payment> findByIdAndStatus(UUID paymentId, PaymentStatus paymentStatus);

    @Query("""
            select distinct new com.vinayms.razorpayclone.payment.repository.SettleablePaymentGroup(
                p.merchantId, p.amount.currency)
            from Payment p
            where p.status = :status
              and p.capturedAt is not null
              and p.capturedAt < :cutoff
              and not exists (
                  select 1 from SettlementPayment sp where sp.id.paymentId = p.id
              )
            """)
    List<SettleablePaymentGroup> findSettleableGroups(
            @Param("status") PaymentStatus status,
            @Param("cutoff") LocalDateTime cutoff);

    @Query("""
            select p from Payment p
            where p.merchantId = :merchantId
              and p.amount.currency = :currency
              and p.status = :status
              and p.capturedAt is not null
              and p.capturedAt < :cutoff
              and not exists (
                  select 1 from SettlementPayment sp where sp.id.paymentId = p.id
              )
            """)
    List<Payment> findSettleablePayments(
            @Param("merchantId") UUID merchantId,
            @Param("currency") String currency,
            @Param("status") PaymentStatus status,
            @Param("cutoff") LocalDateTime cutoff);
}
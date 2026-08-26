package com.vinayms.razorpayclone.payment.repository;

import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrder_Id(UUID orderId);

    Optional<Payment> findByIdAndMerchantId(String paymentId, UUID merchantId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    Optional<Payment> findByIdAndStatus(UUID paymentId, PaymentStatus paymentStatus);
}
package com.vinayms.razorpayclone.payment.repository;

import com.vinayms.razorpayclone.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
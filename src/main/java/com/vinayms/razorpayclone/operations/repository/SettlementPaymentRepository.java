package com.vinayms.razorpayclone.operations.repository;

import com.vinayms.razorpayclone.operations.entity.SettlementPayment;
import com.vinayms.razorpayclone.operations.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {

    List<SettlementPayment> findByIdSettlementId(UUID settlementId);
}

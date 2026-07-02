package com.vinayms.razorpayclone.payment.repository;

import com.vinayms.razorpayclone.payment.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    public boolean existsByMerchantIdAndReceipt(UUID merchantId, String receipt);
}
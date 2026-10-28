package com.vinayms.razorpayclone.payment.repository;

import com.vinayms.razorpayclone.payment.dto.order.request.OrderResponse;
import com.vinayms.razorpayclone.payment.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    public boolean existsByMerchantIdAndReceipt(UUID merchantId, String receipt);

    Optional<Order> findByIdAndMerchantId(UUID orderId, UUID merchantId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.merchantId = :merchantId")
    Optional<Order> findByIdAndMerchantIdForUpdate(UUID orderId, UUID merchantId);
}
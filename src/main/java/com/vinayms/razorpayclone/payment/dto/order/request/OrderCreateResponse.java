package com.vinayms.razorpayclone.payment.dto.order.request;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderCreateResponse(
        UUID id,
        Money amount,
        int attempts,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        
        Map<String, String> notes,
        String receipt,
        LocalDateTime expiresAt
) {
}

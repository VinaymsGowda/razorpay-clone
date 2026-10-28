package com.vinayms.razorpayclone.payment.event;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.OrderStatus;

import java.util.UUID;

public record OrderEventPayload(
        UUID orderId,
        UUID merchantId,
        OrderStatus orderStatus,
        Money amount
) {
}

package com.vinayms.razorpayclone.payment.event;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;

import java.util.UUID;

public record PaymentEventPayload(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod paymentMethod
) {
}

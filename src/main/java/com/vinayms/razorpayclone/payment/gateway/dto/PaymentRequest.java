package com.vinayms.razorpayclone.payment.gateway.dto;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod paymentMethod,
        Map<String,Object> methodDetails
) {
}

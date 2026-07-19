package com.vinayms.razorpayclone.payment.dto.payment.request;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(
        @NotNull(message = "Order Id is required")
        UUID orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        @NotNull(message = "Method details are required")
        Map<String,Object> methodDetails


) {
}

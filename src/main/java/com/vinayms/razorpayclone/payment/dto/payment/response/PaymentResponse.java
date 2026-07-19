package com.vinayms.razorpayclone.payment.dto.payment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse (
        UUID id,
        UUID orderId,
        UUID merchantId,
        PaymentStatus status,
        Money amount,
        PaymentMethod paymentMethod,
        Map<String,Object> methodDetails,
        String errorCode,
        String errorReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){


}

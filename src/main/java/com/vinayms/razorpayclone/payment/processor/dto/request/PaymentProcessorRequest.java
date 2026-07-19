package com.vinayms.razorpayclone.payment.processor.dto.request;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID processingId,
        UUID paymentId,
        PaymentMethod paymentMethod,
        Money amount,
        Map<String,Object> methodDetails,
        String pan,
        String expiry
){

    public static PaymentProcessorRequest card(
            UUID paymentId,
            String pan,
            String expiry,
            Money amount,
            Map<String,Object> methodDetails
    ){
        return new PaymentProcessorRequest(
                UUID.randomUUID(),
                paymentId,
                PaymentMethod.CARD,
                amount,
                methodDetails,
                pan,
                expiry
        );
    }

    public static PaymentProcessorRequest nonCard(
            UUID paymentId,
            Money amount,
            PaymentMethod paymentMethod,
            Map<String,Object> methodDetails
    ){
        return new PaymentProcessorRequest(
                UUID.randomUUID(),
                paymentId,
                paymentMethod,
                amount,
                methodDetails,
                null,
                null
        );
    }
}

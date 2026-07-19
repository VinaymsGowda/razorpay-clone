package com.vinayms.razorpayclone.payment.processor;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorFactory {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public PaymentProcessor getPaymentProcessor(PaymentMethod paymentMethod) {
        if (!paymentProcessors.containsKey(paymentMethod)) {
            throw new BadRequestException("PaymentMethod " + paymentMethod + " not found");
        }

        return paymentProcessors.get(paymentMethod);
    }
}

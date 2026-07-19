package com.vinayms.razorpayclone.payment.gateway;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> paymentStrategies;

    public PaymentStrategy getPaymentAdapter(PaymentMethod paymentMethod) {
        if(!paymentStrategies.containsKey(paymentMethod)){
            throw new BadRequestException("PaymentMethod " + paymentMethod + " not found");
        }

        return paymentStrategies.get(paymentMethod);
    }
}

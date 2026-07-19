package com.vinayms.razorpayclone.payment.config;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.payment.gateway.PaymentStrategy;
import com.vinayms.razorpayclone.payment.gateway.strategy.CardPaymentStrategy;
import com.vinayms.razorpayclone.payment.gateway.strategy.NetBankingStrategy;
import com.vinayms.razorpayclone.payment.gateway.strategy.UpiStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterMapper {
    private final CardPaymentStrategy cardPaymentStrategy;
    private final NetBankingStrategy netBankingStrategy;
    private final UpiStrategy upiStrategy;

    @Bean
    public Map<PaymentMethod, PaymentStrategy> getPaymentAdapterMapper() {
        Map<PaymentMethod, PaymentStrategy> paymentAdapterMap=new HashMap<>();
        paymentAdapterMap.put(PaymentMethod.CARD, cardPaymentStrategy);
        paymentAdapterMap.put(PaymentMethod.NETBANKING, netBankingStrategy);
        paymentAdapterMap.put(PaymentMethod.UPI, upiStrategy);
        return paymentAdapterMap;
    }
}

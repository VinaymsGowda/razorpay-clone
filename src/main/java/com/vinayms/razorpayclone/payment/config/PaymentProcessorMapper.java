package com.vinayms.razorpayclone.payment.config;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.adapter.CardPaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.adapter.NetBankingProcessor;
import com.vinayms.razorpayclone.payment.processor.adapter.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorMapper {
    private final CardPaymentProcessor cardPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;
    private final NetBankingProcessor netBankingProcessor;
    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessors() {
        Map<PaymentMethod, PaymentProcessor> processorMap = new HashMap<>();
        processorMap.put(PaymentMethod.CARD, cardPaymentProcessor);
        processorMap.put(PaymentMethod.UPI, upiPaymentProcessor);
        processorMap.put(PaymentMethod.NETBANKING, netBankingProcessor);
        
        return processorMap;
    }
}

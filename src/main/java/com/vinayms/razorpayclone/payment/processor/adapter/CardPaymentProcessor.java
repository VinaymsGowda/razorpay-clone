package com.vinayms.razorpayclone.payment.processor.adapter;

import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private static final String PAN_CARD_DECLINED="4000000000000002";
    private static final String PAN_CARD_EXPIRED="4000000000000003";
    @Override
    public PaymentProcessorResponse processPayment(PaymentProcessorRequest paymentRequest) {
        // TODO: Implement card payment processing logic
        String pan=paymentRequest.pan();

        if(PAN_CARD_DECLINED.equals(pan)){
            log.warn("Bank rejected the payment - {}",paymentRequest.paymentId());
            throw new BadRequestException("Bank rejected the payment");
        }
        if(PAN_CARD_EXPIRED.equals(pan)){
            log.warn("Bank rejected the payment for PAN due to expired card for payment : {}", paymentRequest.paymentId());
            throw new BadRequestException("Bank rejected the payment due to expired card");
        }

        String processorRef= "CARD_"+RandomizerUtil.randomBase64(16);

        String redirectRef="http://www.google.com/npci/"+processorRef;

        return new PaymentProcessorResponse.Pending(
                processorRef
        );
    }
}

package com.vinayms.razorpayclone.payment.processor.adapter;

import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetBankingProcessor implements PaymentProcessor {


    private final String BANK_CODE_FAIL="BANK_CODE_FAIL";
    @Override
    public PaymentProcessorResponse processPayment(PaymentProcessorRequest paymentRequest) {
        // TODO: Implement net banking payment processing logic

        String bankCode=paymentRequest.methodDetails()!=null?
                paymentRequest.methodDetails().get("BANK_CODE").toString():null;

        if(BANK_CODE_FAIL.equals(bankCode)){
            throw new BadRequestException("Bank rejected the payment");
        }

        String processorRef= "NETBANK_"+RandomizerUtil.randomBase64(16);

        String redirectRef="http://www.google.com/netbanking/"+processorRef;

        return new PaymentProcessorResponse.Pending(
                processorRef
        );
    }
}

package com.vinayms.razorpayclone.payment.processor.adapter;

import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpiPaymentProcessor implements PaymentProcessor {

    private final String VPA_FAIL="fail@hdfc.in";
    @Override
    public PaymentProcessorResponse processPayment(PaymentProcessorRequest paymentRequest) {
        // TODO: Implement net banking payment processing logic

        String vpa=paymentRequest.methodDetails()!=null?
                paymentRequest.methodDetails().get("vpa").toString():null;

        if(VPA_FAIL.equals(vpa)){
            throw new BadRequestException("Bank rejected the payment");
        }

        String processorRef= "UPI_"+RandomizerUtil.randomBase64(16);

        String redirectRef="http://www.google.com/npci/"+processorRef;

        return new PaymentProcessorResponse.Pending(
                processorRef
        );
    }
}

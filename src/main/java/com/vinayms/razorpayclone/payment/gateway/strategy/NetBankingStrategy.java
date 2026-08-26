package com.vinayms.razorpayclone.payment.gateway.strategy;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.payment.gateway.PaymentStrategy;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessorFactory;
import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetBankingStrategy implements PaymentStrategy {

    private final PaymentProcessorFactory paymentProcessorFactory;
    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        // Implement net banking payment initiation logic here
        log.info("Initiating Netbanking payment Adapter {} {}",1,2);

        try {


        PaymentProcessor processor=paymentProcessorFactory.getPaymentProcessor(
                paymentRequest.paymentMethod()
        );
        PaymentProcessorRequest processorRequest=PaymentProcessorRequest.nonCard(
                paymentRequest.paymentId(),
                paymentRequest.amount(),
                PaymentMethod.NETBANKING,
                paymentRequest.methodDetails()
        );
        PaymentProcessorResponse processorResponse = processor.processPayment(processorRequest);

        return switch (processorResponse){
            case PaymentProcessorResponse.Pending pendingResponse ->
                    new PaymentResult.Pending(
                        pendingResponse.processorRef()
                );

            case PaymentProcessorResponse.Success successResponse ->
                 new PaymentResult.Success(
                        successResponse.bankReference()
                );
            case PaymentProcessorResponse.Failed failureResponse ->
                 new PaymentResult.Failed
                        (failureResponse.errorCode(),failureResponse.errorDescription());

        };

        }catch (Exception e){
            log.error("Error while initiating netbanking payment Adapter",e);
            return new PaymentResult.Failed
                    ("NETBANKING_INIT_ERROR",e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NBK_SUCCESS_REF");

    }
}

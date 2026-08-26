package com.vinayms.razorpayclone.payment.gateway.strategy;

import com.vinayms.razorpayclone.payment.gateway.PaymentStrategy;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentResult;
import com.vinayms.razorpayclone.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy {

    private final VaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {

        String token = (String) paymentRequest.methodDetails().get("token");
        PaymentProcessorResponse paymentProcessorResponse = vaultService.charge(paymentRequest.paymentId(), token, paymentRequest.amount(), paymentRequest.methodDetails()
                , paymentRequest.paymentMethod()
        );

        PaymentResult result = null;

        switch (paymentProcessorResponse) {
            case PaymentProcessorResponse.Pending pending -> {
                result = new PaymentResult.Pending(
                        pending.processorRef()
                );
            }
            case PaymentProcessorResponse.Success success -> {
                result = new PaymentResult.Success(
                        success.bankReference()
                );
            }
            case PaymentProcessorResponse.Failed failed -> {
                result = new PaymentResult.Failed(
                        failed.errorCode(),
                        failed.errorDescription()
                );
            }

        }
        return result;
    }
    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_SUCCESS_REF");
    }
}

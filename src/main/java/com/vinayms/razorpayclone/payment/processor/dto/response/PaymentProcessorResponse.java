package com.vinayms.razorpayclone.payment.processor.dto.response;


public sealed interface PaymentProcessorResponse permits PaymentProcessorResponse.Pending, PaymentProcessorResponse.Success, PaymentProcessorResponse.Failed {
    record Pending(String processorRef) implements PaymentProcessorResponse {
    }

    record Success(String processorRef, String bankReference) implements PaymentProcessorResponse {
    }

    record Failed(String errorCode, String errorDescription) implements PaymentProcessorResponse {
    }
}

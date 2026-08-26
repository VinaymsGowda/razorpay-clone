package com.vinayms.razorpayclone.payment.gateway.dto;



public sealed interface PaymentResult permits PaymentResult.Pending, PaymentResult.Failed,PaymentResult.Success {
    record Pending(String registrationRef) implements PaymentResult {
    }

    record Failed(String errorCode, String errorDescription) implements PaymentResult {
    }

    record Success(String bankReference) implements PaymentResult {
    }
}
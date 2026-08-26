package com.vinayms.razorpayclone.payment.processor;

import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;

public interface PaymentProcessor {

    public PaymentProcessorResponse processPayment(PaymentProcessorRequest paymentRequest);
}

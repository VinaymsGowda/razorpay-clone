package com.vinayms.razorpayclone.payment.processor;

import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentResult;

public interface PaymentProcessor {

    public PaymentProcessorResponse processPayment(PaymentProcessorRequest paymentRequest);
}

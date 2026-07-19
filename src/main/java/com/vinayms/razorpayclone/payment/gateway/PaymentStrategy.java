package com.vinayms.razorpayclone.payment.gateway;

import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentResult;

import java.util.UUID;

public interface PaymentStrategy {

   PaymentResult initiate(PaymentRequest paymentRequest);

   PaymentResult capture(UUID paymentId);
}

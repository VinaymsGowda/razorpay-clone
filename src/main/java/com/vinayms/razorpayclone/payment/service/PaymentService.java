package com.vinayms.razorpayclone.payment.service;

import com.vinayms.razorpayclone.payment.dto.payment.request.PaymentInitRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentInitRequest paymentRequest);

    PaymentResponse capturePayment(UUID merchantId, String paymentId);

    void resolveAuthorization(UUID id, boolean approve, String bankRef, String ErrCode, String errorDescription);
}

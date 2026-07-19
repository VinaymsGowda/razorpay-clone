package com.vinayms.razorpayclone.payment.controller;

import com.vinayms.razorpayclone.payment.dto.payment.request.PaymentInitRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/payments")
public class PaymentController {

    //TODO: Add authentication and read merchandId from auth object
    private final UUID merchantId=UUID.fromString("7d449594-9d7c-4b32-9531-60165a7bcdfa");

    private final PaymentService paymentService;

    // This api is called when user fills in payment details and clicks pay now in gateway
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody @Valid PaymentInitRequest paymentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(paymentRequest));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capturePayment(@PathVariable("paymentId") String paymentId) {
        return ResponseEntity.ok(paymentService.capturePayment(merchantId,paymentId));
    }
}

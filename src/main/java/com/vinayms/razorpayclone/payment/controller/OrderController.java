package com.vinayms.razorpayclone.payment.controller;

import com.vinayms.razorpayclone.merchant.security.MerchantContext;
import com.vinayms.razorpayclone.payment.dto.order.request.OrderResponse;
import com.vinayms.razorpayclone.payment.dto.order.response.OrderCreateRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {


    private final OrderService orderService;
    private final MerchantContext merchantContext;

    @PostMapping(path = "")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest orderReq){
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.ok(orderService.createOrder(merchantId, orderReq));
    }

    @PostMapping(path = "/cancel/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.ok(orderService.cancelOrder(merchantId, orderId));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.ok(orderService.getById(merchantId, id));
    }

    @GetMapping(path = "/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.ok(orderService.getPaymentById(merchantId, paymentId));
    }
}

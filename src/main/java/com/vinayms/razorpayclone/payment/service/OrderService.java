package com.vinayms.razorpayclone.payment.service;

import com.vinayms.razorpayclone.payment.dto.order.request.OrderResponse;
import com.vinayms.razorpayclone.payment.dto.order.response.OrderCreateRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    public OrderResponse createOrder(UUID merchantId, OrderCreateRequest orderCreateRequest);

    public OrderResponse cancelOrder(UUID merchantId, UUID orderId);

    public OrderResponse getById(UUID merchantId, UUID orderId);

    List<PaymentResponse> getPayments(UUID merchantId, UUID orderId);



}

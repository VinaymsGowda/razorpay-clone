package com.vinayms.razorpayclone.payment.service;

import com.vinayms.razorpayclone.payment.dto.order.request.OrderCreateResponse;
import com.vinayms.razorpayclone.payment.dto.order.response.OrderCreateRequest;

import java.util.UUID;

public interface OrderService {

    public OrderCreateResponse createOrder(UUID merchantId,OrderCreateRequest orderCreateRequest);

}

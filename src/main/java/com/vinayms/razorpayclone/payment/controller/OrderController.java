package com.vinayms.razorpayclone.payment.controller;

import com.vinayms.razorpayclone.payment.dto.order.request.OrderResponse;
import com.vinayms.razorpayclone.payment.dto.order.response.OrderCreateRequest;
import com.vinayms.razorpayclone.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    //TODO: Add authentication and read merchandId from auth object
    private final UUID merchantId=UUID.fromString("7d449594-9d7c-4b32-9531-60165a7bcdfa");

    private final OrderService orderService;

    @PostMapping(path = "")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest orderReq){
        return ResponseEntity.ok(orderService.createOrder(merchantId, orderReq));
    }
}

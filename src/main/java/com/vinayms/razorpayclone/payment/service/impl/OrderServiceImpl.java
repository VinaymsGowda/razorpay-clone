package com.vinayms.razorpayclone.payment.service.impl;

import com.vinayms.razorpayclone.common.enums.OrderStatus;
import com.vinayms.razorpayclone.common.exceptions.DuplicateResourceException;
import com.vinayms.razorpayclone.payment.dto.order.request.OrderCreateResponse;
import com.vinayms.razorpayclone.payment.dto.order.response.OrderCreateRequest;
import com.vinayms.razorpayclone.payment.entity.Order;
import com.vinayms.razorpayclone.payment.repository.OrderRepository;
import com.vinayms.razorpayclone.payment.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;


    @Transactional
    @Override
    public OrderCreateResponse createOrder(UUID merchantId, OrderCreateRequest orderCreateRequest) {
        if(orderCreateRequest.receipt()!=null
                && orderRepository.
                existsByMerchantIdAndReceipt(merchantId, orderCreateRequest.receipt())) {
            log.warn("Order already exists for merchant: {} and receipt: {}", merchantId, orderCreateRequest.receipt());
            throw new DuplicateResourceException("Order already exists for merchant: " + merchantId + " and receipt: " + orderCreateRequest.receipt());
        }


        LocalDateTime expiresAt=orderCreateRequest.expiresAt()!=null
                ?orderCreateRequest.expiresAt():
                LocalDateTime.now().plusMinutes(15);
        Order order = Order
                .builder()
                .amount(orderCreateRequest.amount())
                .receipt(orderCreateRequest.receipt())
                .notes(orderCreateRequest.notes())
                .merchantId(merchantId)
                .attempts(0)
                .status(OrderStatus.CREATED)
                .expiresAt(expiresAt)
                .build();
        order=orderRepository.save(order);

        return new OrderCreateResponse(
                order.getId(),
                order.getAmount(),
                order.getAttempts(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getNotes(),
                order.getReceipt(),
                order.getExpiresAt()
        );
    }
}

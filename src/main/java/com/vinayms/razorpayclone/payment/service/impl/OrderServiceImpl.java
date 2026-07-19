package com.vinayms.razorpayclone.payment.service.impl;

import com.vinayms.razorpayclone.common.enums.OrderStatus;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.exceptions.DuplicateResourceException;
import com.vinayms.razorpayclone.payment.dto.order.request.OrderResponse;
import com.vinayms.razorpayclone.payment.dto.order.response.OrderCreateRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.entity.Order;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.mapper.OrderMapper;
import com.vinayms.razorpayclone.payment.mapper.PaymentMapper;
import com.vinayms.razorpayclone.payment.repository.OrderRepository;
import com.vinayms.razorpayclone.payment.repository.PaymentRepository;
import com.vinayms.razorpayclone.payment.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional()
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Transactional
    @Override
    public OrderResponse createOrder(UUID merchantId, OrderCreateRequest orderCreateRequest) {
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

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(UUID merchantId, UUID orderId) {
        Order order=orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(
                ()->new ResolutionException("Order with id not found! "+orderId)
        );

        if(order.getStatus()==OrderStatus.CANCELLED || order.getStatus()==OrderStatus.PAID){
            log.warn("Order already cancelled for merchant: {} and orderId: {}", merchantId, orderId);
            throw new BadRequestException("Order     already cancelled for merchant: " + merchantId + " and orderId: " + orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order=orderRepository.save(order);

        return orderMapper.toOrderResponse(order);
    }

    @Override

    public OrderResponse getById(UUID merchantId, UUID orderId) {
        Order order=orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(
                ()->new ResolutionException("Order with id not found! "+orderId)
        );

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<PaymentResponse> getPayments(UUID merchantId, UUID orderId) {
        Order order=orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(
                ()->new ResolutionException("Order with id not found! "+orderId)
        );

        List<Payment> payments=paymentRepository.findByOrder_Id(orderId);

       return paymentMapper.toPaymentResponseList(payments);

    }
}

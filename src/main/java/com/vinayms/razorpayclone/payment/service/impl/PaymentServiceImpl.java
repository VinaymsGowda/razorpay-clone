package com.vinayms.razorpayclone.payment.service.impl;

import com.vinayms.razorpayclone.common.enums.EventAggregateType;
import com.vinayms.razorpayclone.common.enums.OrderStatus;
import com.vinayms.razorpayclone.common.enums.OutboxEventType;
import com.vinayms.razorpayclone.common.enums.PaymentEvent;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.common.outbox.OutboxService;
import com.vinayms.razorpayclone.merchant.security.MerchantContext;
import com.vinayms.razorpayclone.payment.dto.payment.request.PaymentInitRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.entity.Order;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.event.PaymentEventPayload;
import com.vinayms.razorpayclone.payment.gateway.PaymentStrategy;
import com.vinayms.razorpayclone.payment.gateway.PaymentStrategyFactory;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.vinayms.razorpayclone.payment.mapper.PaymentMapper;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentResult;
import com.vinayms.razorpayclone.payment.repository.OrderRepository;
import com.vinayms.razorpayclone.payment.repository.PaymentRepository;
import com.vinayms.razorpayclone.payment.repository.PaymentTransitionLogRepository;
import com.vinayms.razorpayclone.payment.service.PaymentService;
import com.vinayms.razorpayclone.payment.service.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.module.ResolutionException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService transitionService;
    private final MerchantContext merchantContext;
    private final OutboxService outboxService;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PaymentResponse initiatePayment(PaymentInitRequest paymentRequest) {

        Order order=orderRepository.findByIdAndMerchantIdForUpdate(paymentRequest.orderId(),merchantContext.getMerchantId())
                .orElseThrow(()->new RuntimeException("Order not found for id: "+paymentRequest.orderId()));


        OrderStatus orderStatus=order.getStatus();
        if(orderStatus.equals(OrderStatus.PAID) || orderStatus.equals(OrderStatus.CANCELLED)){
            throw new BadRequestException("Order cannot accept payment for status : "+orderStatus);
        }

        order.setStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment=Payment.builder()
                .paymentMethod(paymentRequest.method())
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .idempotencyKey(UUID.randomUUID().toString())
                .order(order)
                .merchantId(order.getMerchantId())
                .methodDetails(paymentRequest.methodDetails())
                .build();

        payment=paymentRepository.save(payment);

        PaymentStrategy paymentAdapter= paymentStrategyFactory.getPaymentAdapter(paymentRequest.method());
        if(paymentAdapter==null){
            throw new BadRequestException("Payment Adapter not found for "+paymentRequest.method());
        }
        PaymentRequest request=paymentMapper.toPaymentRequest(payment);
        transitionService.createTransition(payment,PaymentEvent.AUTHORIZE_ATTEMPT);

        // This will be n/w call in Real environment adding it inside a Pessimistic locking transaction will cause connection pool error

        // TODO: Add Outbox pattern to do network call outside
        PaymentResult paymentResult=paymentAdapter.initiate(request);

        switch (paymentResult){
            case PaymentResult.Pending pending -> {
               payment.setProcessorReference(pending.registrationRef());
            }
            case PaymentResult.Failed failed -> {
                transitionService.createTransition(payment,PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorReason(failed.errorDescription());
                payment.setErrorCode(failed.errorCode());
            }
            case PaymentResult.Success success -> {
                payment.setBankReference(success.bankReference());
            }
        }
        payment=paymentRepository.save(payment);
        orderRepository.save(order);
        recordPaymentEvent(payment, OutboxEventType.PAYMENT_INITIATED);

        return paymentMapper.toPaymentResponse(payment);

    }

    @Override
    @Transactional
    public PaymentResponse capturePayment(UUID merchantId, UUID paymentId) {
        Payment payment=paymentRepository.findByIdAndMerchantId(paymentId,merchantId)
                .orElseThrow(() ->new ResolutionException("Payment not found for id: "+paymentId));

        transitionService.createTransition(payment,PaymentEvent.CAPTURE_REQUEST);


        PaymentStrategy paymentAdapter= paymentStrategyFactory.getPaymentAdapter(payment.getPaymentMethod());

        PaymentResult paymentResult=paymentAdapter.capture(payment.getId());
        OutboxEventType eventType = null;
        if(paymentResult instanceof PaymentResult.Success success){
            log.info("Payment captured successfully for paymentId: {} and merchantId: {}",paymentId,merchantId);
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setProcessorReference(success.bankReference());
            transitionService.createTransition(payment,PaymentEvent.CAPTURE_SUCCESS);
            eventType = OutboxEventType.PAYMENT_CAPTURED;
        }else if(paymentResult instanceof PaymentResult.Failed(String errorCode, String errorDescription)) {
            log.warn("Payment capture failed for paymentId: {} and merchantId: {}",paymentId,merchantId);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorCode(errorCode);

            payment.setErrorReason(errorDescription);
            transitionService.createTransition(payment,PaymentEvent.CAPTURE_FAIL);
            eventType = OutboxEventType.PAYMENT_FAILED;
        }
        payment=paymentRepository.save(payment);
        if (eventType != null) {
            recordPaymentEvent(payment, eventType);
        }
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String ErrCode, String errorDescription) {
        Payment payment=paymentRepository.findById(paymentId).orElseThrow(
                ()->new ResourceNotFoundException("Payment not found for id: "+paymentId)
        );

        if(payment.getStatus()!=PaymentStatus.AUTHORIZING){
            log.warn("Payment is not in Authorizing state for {} ,status : {} ", payment.getId(),payment.getStatus());
        }

        Order order=payment.getOrder();
        OutboxEventType eventType = null;

        if(approve){
            transitionService.createTransition(payment,PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);

            // Auto capture

            transitionService.createTransition(payment,PaymentEvent.CAPTURE_REQUEST);

            PaymentStrategy paymentStrategy=paymentStrategyFactory.getPaymentAdapter(
                    payment.getPaymentMethod()
            );
            PaymentResult captureResult=paymentStrategy.capture(paymentId);
            if(captureResult instanceof PaymentResult.Success){
                transitionService.createTransition(payment,PaymentEvent.CAPTURE_SUCCESS);
                order.setStatus(OrderStatus.PAID);
                eventType = OutboxEventType.PAYMENT_CAPTURED;
            }
            else if(captureResult instanceof PaymentResult.Failed){
                transitionService.createTransition(payment,PaymentEvent.CAPTURE_FAIL);
                payment.setErrorReason(errorDescription);
                payment.setErrorCode(ErrCode);
                eventType = OutboxEventType.PAYMENT_FAILED;
            }
        }else{
            transitionService.createTransition(payment,PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorReason(errorDescription);
            payment.setErrorCode(ErrCode);
            eventType = OutboxEventType.PAYMENT_FAILED;
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
        if (eventType != null) {
            recordPaymentEvent(payment, eventType);
        }

        // ToDo: setup a message broker for notifying merchant backend webhooks

    }

    private void recordPaymentEvent(Payment payment, OutboxEventType eventType) {
        outboxService.record(
                EventAggregateType.PAYMENT,
                payment.getId(),
                eventType.name(),
                new PaymentEventPayload(
                        payment.getId(),
                        payment.getOrder().getId(),
                        payment.getMerchantId(),
                        payment.getAmount(),
                        payment.getPaymentMethod()
                )
        );
    }
}

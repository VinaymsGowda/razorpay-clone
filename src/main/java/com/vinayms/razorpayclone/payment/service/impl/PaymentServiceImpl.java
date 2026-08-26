package com.vinayms.razorpayclone.payment.service.impl;

import com.vinayms.razorpayclone.common.enums.OrderStatus;
import com.vinayms.razorpayclone.common.enums.PaymentEvent;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.payment.dto.payment.request.PaymentInitRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.entity.Order;
import com.vinayms.razorpayclone.payment.entity.Payment;
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

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PaymentResponse initiatePayment(PaymentInitRequest paymentRequest) {

        Order order=orderRepository.findById(paymentRequest.orderId())
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


    return paymentMapper.toPaymentResponse(payment);

    }

    @Override
    public PaymentResponse capturePayment(UUID merchantId, String paymentId) {
        Payment payment=paymentRepository.findByIdAndMerchantId(paymentId,merchantId)
                .orElseThrow(() ->new ResolutionException("Payment not found for id: "+paymentId));

        transitionService.createTransition(payment,PaymentEvent.CAPTURE_REQUEST);


        PaymentStrategy paymentAdapter= paymentStrategyFactory.getPaymentAdapter(payment.getPaymentMethod());

        PaymentResult paymentResult=paymentAdapter.capture(payment.getId());
        if(paymentResult instanceof PaymentResult.Success success){
            log.info("Payment captured successfully for paymentId: {} and merchantId: {}",paymentId,merchantId);
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setProcessorReference(success.bankReference());
            transitionService.createTransition(payment,PaymentEvent.CAPTURE_SUCCESS);
        }else if(paymentResult instanceof PaymentResult.Failed(String errorCode, String errorDescription)) {
            log.warn("Payment capture failed for paymentId: {} and merchantId: {}",paymentId,merchantId);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorCode(errorCode);

            payment.setErrorReason(errorDescription);
            transitionService.createTransition(payment,PaymentEvent.CAPTURE_FAIL);
        }
        payment=paymentRepository.save(payment);
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
            }
            else if(captureResult instanceof PaymentResult.Failed){
                transitionService.createTransition(payment,PaymentEvent.CAPTURE_FAIL);
                payment.setErrorReason(errorDescription);
                payment.setErrorCode(ErrCode);
            }
        }else{
            transitionService.createTransition(payment,PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorReason(errorDescription);
            payment.setErrorCode(ErrCode);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);


        // ToDo: setup a message broker for notifying merchant backend webhooks

    }
}

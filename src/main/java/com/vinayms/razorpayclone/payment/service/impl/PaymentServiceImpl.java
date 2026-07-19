package com.vinayms.razorpayclone.payment.service.impl;

import com.vinayms.razorpayclone.common.enums.OrderStatus;
import com.vinayms.razorpayclone.common.enums.PaymentActor;
import com.vinayms.razorpayclone.common.enums.PaymentEvent;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.payment.dto.payment.request.PaymentInitRequest;
import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.entity.Order;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.entity.PaymentTransitionLog;
import com.vinayms.razorpayclone.payment.gateway.PaymentStrategy;
import com.vinayms.razorpayclone.payment.gateway.PaymentStrategyFactory;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.vinayms.razorpayclone.payment.mapper.PaymentMapper;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentResult;
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
import java.time.LocalDateTime;
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
                .status(PaymentStatus.INITIATED)
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
                payment.setProcessorReference(success.processorRef());

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
            payment.setProcessorReference(success.processorRef());
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
}

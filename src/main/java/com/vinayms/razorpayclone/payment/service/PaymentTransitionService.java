package com.vinayms.razorpayclone.payment.service;

import com.vinayms.razorpayclone.common.enums.PaymentActor;
import com.vinayms.razorpayclone.common.enums.PaymentEvent;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.entity.PaymentTransitionLog;
import com.vinayms.razorpayclone.payment.repository.PaymentTransitionLogRepository;
import com.vinayms.razorpayclone.payment.statemachine.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentStateMachine paymentStateMachine;
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;


    public void createTransition(Payment payment, PaymentEvent event){
        PaymentStatus next=paymentStateMachine.transition(payment.getStatus(),event);

        PaymentTransitionLog transitionLog=
                PaymentTransitionLog.builder()
                        .actor(PaymentActor.SYSTEM)      // ToDo: Fetch data from context
                        .fromStatus(payment.getStatus())
                        .toStatus(next)
                        .event(event)
                        .occurredAt(LocalDateTime.now())
                        .payment(payment)
                        .build();
        paymentTransitionLogRepository.save(transitionLog);
        payment.setStatus(next);




    }

}

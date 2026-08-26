package com.vinayms.razorpayclone.payment.statemachine;

import com.vinayms.razorpayclone.common.enums.PaymentEvent;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentStateMachine {


    private record StateTransition(PaymentStatus from, PaymentEvent to) {

    }
    private static final Map<StateTransition,PaymentStatus> TRANSITION=Map.ofEntries(
            Map.entry(new StateTransition(PaymentStatus.CREATED, PaymentEvent.AUTHORIZE_ATTEMPT),PaymentStatus.AUTHORIZING),
            Map.entry(new StateTransition(PaymentStatus.CREATED,PaymentEvent.AUTHORIZE_FAIL),PaymentStatus.FAILED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_SUCCESS),PaymentStatus.AUTHORIZED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZING,PaymentEvent.AUTHORIZE_FAIL),PaymentStatus.FAILED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZED,PaymentEvent.CAPTURE_REQUEST),PaymentStatus.CAPTURING),
            Map.entry(new StateTransition(PaymentStatus.CAPTURING,PaymentEvent.CAPTURE_SUCCESS),PaymentStatus.CAPTURED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURING,PaymentEvent.CAPTURE_FAIL),PaymentStatus.AUTHORIZED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURED,PaymentEvent.SETTLE),PaymentStatus.SETTLED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURED,PaymentEvent.REFUND_INIT),PaymentStatus.PARTIAL_REFUND),
            Map.entry(new StateTransition(PaymentStatus.SETTLED,PaymentEvent.REFUND_INIT),PaymentStatus.PARTIAL_REFUND),
            Map.entry(new StateTransition(PaymentStatus.PARTIAL_REFUND,PaymentEvent.REFUND_COMPLETE),PaymentStatus.REFUNDED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURED,PaymentEvent.REFUND_COMPLETE),PaymentStatus.REFUNDED),

            Map.entry(new StateTransition(PaymentStatus.CREATED,PaymentEvent.CANCEL),PaymentStatus.CANCELLED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZING,PaymentEvent.CANCEL),PaymentStatus.CANCELLED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZED,PaymentEvent.CAPTURE_TIMEOUT),PaymentStatus.AUTH_EXPIRED)


    );

    public PaymentStatus transition(PaymentStatus current,PaymentEvent event){
        PaymentStatus next=TRANSITION.get(new StateTransition(current,event));

        if(next==null){
            throw new BadRequestException("Invalid state transition "+current+" to "+event+" not allowed");
        }
        return next;
    }

}

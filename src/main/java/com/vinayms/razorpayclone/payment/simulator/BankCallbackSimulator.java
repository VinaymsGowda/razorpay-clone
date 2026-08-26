package com.vinayms.razorpayclone.payment.simulator;

import com.vinayms.razorpayclone.common.enums.ChaosMode;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.repository.PaymentRepository;
import com.vinayms.razorpayclone.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks() {


        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        log.info("Found {} payments", candidates.size());

        if (candidates.isEmpty()) return;

        for (Payment payment: candidates) {
            simulateCallback(payment);
        }

    }

    private void simulateCallback(Payment payment) {
     SimulatorConfig.MethodSimulatorConfig methodConfig= simulatorConfig.getSimulatorConfig(payment.getPaymentMethod());


     LocalDateTime dueAt=dueAt(payment,methodConfig);

     if(LocalDateTime.now().isBefore(dueAt)){
         return;
     }


     ChaosMode chaosMode=simulatorConfig.getChaosMode();

     switch (chaosMode) {
         case SUCCESS -> resolvePayment(payment,true);
         case FAILURE -> resolvePayment(payment,false);
         case TIMEOUT -> log.warn("Payment {} timeout ", payment.getId());
         case NORMAL,SLOW -> resolvePayment(payment, resolvePaymentStatus(payment, methodConfig));

     }
    }

    private boolean resolvePaymentStatus(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig){
        int bucket=Math.abs(payment.getId().hashCode())%100;

        return bucket<methodSimulatorConfig.getSuccessRate();
    }

    private void resolvePayment(Payment payment, boolean approve) {
        if(approve){
            String bankRef="SIM_BANK_REF"+ RandomizerUtil.randomBase64(16);
            paymentService.resolveAuthorization(payment.getId(),true,bankRef,null,null);
        }else{
            paymentService.resolveAuthorization(payment.getId(),false,null,"SIM_BANK_ERR_CODE","Payment declined due to internal error");
        }
    }


    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig) {
        int range=methodSimulatorConfig.getMaxDelaySeconds()-methodSimulatorConfig.getMinDelaySeconds();

        int delaySeconds=methodSimulatorConfig.getMinDelaySeconds() + (Math.abs(payment.getId().hashCode())%(range+1));

        if(simulatorConfig.getChaosMode()==ChaosMode.SLOW){
            delaySeconds*=2;
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }

}

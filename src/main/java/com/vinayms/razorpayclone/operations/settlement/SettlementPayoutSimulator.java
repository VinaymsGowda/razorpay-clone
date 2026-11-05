package com.vinayms.razorpayclone.operations.settlement;

import com.vinayms.razorpayclone.common.enums.ChaosMode;
import com.vinayms.razorpayclone.common.enums.SettlementStatus;
import com.vinayms.razorpayclone.operations.entity.Settlement;
import com.vinayms.razorpayclone.operations.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementPayoutSimulator {

    private final SettlementRepository settlementRepository;
    private final SettlementPayoutService settlementPayoutService;
    private final SettlementSimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${settlement.simulator.poll-interval-ms:5000}")
    public void processPayouts() {
        List<Settlement> due = settlementRepository.findDuePending(
                SettlementStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, simulatorConfig.getBatchSize()));
        if (due.isEmpty()) {
            return;
        }
        log.info("Found {} pending settlements", due.size());

        for (Settlement settlement : due) {
            try {
                settlementPayoutService.process(settlement.getId(), decide());
            } catch (RuntimeException ex) {
                log.error("Settlement payout failed for {}", settlement.getId(), ex);
            }
        }
    }

    private PayoutDecision decide() {
        ChaosMode chaosMode = simulatorConfig.getChaosMode();
        return switch (chaosMode) {
            case SUCCESS -> PayoutDecision.approved();
            case FAILURE -> PayoutDecision.retryable("BANK_DECLINED", "Settlement payout declined");
            case TIMEOUT -> PayoutDecision.retryable("BANK_TIMEOUT", "Settlement payout timed out");
            case NORMAL, SLOW -> randomDecision();
        };
    }

    private PayoutDecision randomDecision() {
        int outcome = ThreadLocalRandom.current().nextInt(100);
        if (outcome < simulatorConfig.getSuccessRate()) {
            return PayoutDecision.approved();
        }
        if (ThreadLocalRandom.current().nextInt(100) < 70) {
            return PayoutDecision.retryable("BANK_TIMEOUT", "Settlement payout timed out");
        }
        return PayoutDecision.permanent("INVALID_ACCOUNT", "Settlement bank account was rejected");
    }
}

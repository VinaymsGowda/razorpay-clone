package com.vinayms.razorpayclone.operations.settlement;

import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.payment.repository.PaymentRepository;
import com.vinayms.razorpayclone.payment.repository.SettleablePaymentGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final SettlementProperties settlementProperties;
    private final PaymentRepository paymentRepository;
    private final SettlementService settlementService;

    @Scheduled(cron = "${settlement.create-cron:0 0 1 * * *}", zone = "${settlement.zone:Asia/Kolkata}")
    public void createDailySettlements() {
        ZoneId zone = ZoneId.of(settlementProperties.getZone());
        LocalDateTime cutoff = LocalDate.now(zone)
                .minusDays(settlementProperties.getHoldDays())
                .atStartOfDay();
        List<SettleablePaymentGroup> groups = paymentRepository.findSettleableGroups(PaymentStatus.CAPTURED, cutoff);
        log.info("Settlement creation cutoff {} found {} merchant groups", cutoff, groups.size());

        for (SettleablePaymentGroup group : groups) {
            try {
                settlementService.createForMerchant(group.merchantId(), group.currency(), cutoff);
            } catch (RuntimeException ex) {
                log.error("Failed to create settlement for merchant {} currency {}",
                        group.merchantId(), group.currency(), ex);
            }
        }
    }
}

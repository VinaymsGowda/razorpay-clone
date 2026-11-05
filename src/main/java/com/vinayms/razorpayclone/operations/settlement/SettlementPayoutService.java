package com.vinayms.razorpayclone.operations.settlement;

import com.vinayms.razorpayclone.common.enums.EventAggregateType;
import com.vinayms.razorpayclone.common.enums.OutboxEventType;
import com.vinayms.razorpayclone.common.enums.PaymentEvent;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.enums.SettlementStatus;
import com.vinayms.razorpayclone.common.outbox.OutboxService;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.merchant.entity.Merchant;
import com.vinayms.razorpayclone.merchant.repository.MerchantRepository;
import com.vinayms.razorpayclone.operations.entity.Settlement;
import com.vinayms.razorpayclone.operations.entity.SettlementPayment;
import com.vinayms.razorpayclone.operations.event.SettlementEventPayload;
import com.vinayms.razorpayclone.operations.repository.SettlementPaymentRepository;
import com.vinayms.razorpayclone.operations.repository.SettlementRepository;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.repository.PaymentRepository;
import com.vinayms.razorpayclone.payment.service.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementPayoutService {

    private static final String MISSING_ACCOUNT = "MISSING_ACCOUNT";

    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransitionService paymentTransitionService;
    private final MerchantRepository merchantRepository;
    private final OutboxService outboxService;
    private final SettlementProperties settlementProperties;

    @Transactional
    public void process(UUID settlementId, PayoutDecision decision) {
        Settlement settlement = settlementRepository.lockDueById(
                settlementId, SettlementStatus.PENDING, LocalDateTime.now()).orElse(null);
        if (settlement == null) {
            return;
        }

        Merchant merchant = merchantRepository.findById(settlement.getMerchantId()).orElse(null);
        String merchantBankAccount = merchant == null ? null : merchant.getSettlementBankAccountNum();
        if (isBlank(merchantBankAccount)) {
            fail(settlement, null, MISSING_ACCOUNT, "Merchant settlement bank account is missing", false);
            return;
        }

        if (decision.success()) {
            succeed(settlement, merchantBankAccount);
            return;
        }
        fail(settlement, merchantBankAccount, decision.failureCode(), decision.failureReason(), decision.retryable());
    }

    private void succeed(Settlement settlement, String merchantBankAccount) {
        List<UUID> paymentIds = paymentIds(settlement);
        List<Payment> payments = paymentRepository.findAllById(paymentIds);
        if (payments.size() != paymentIds.size()) {
            fail(settlement, merchantBankAccount, "PAYMENT_MISSING", "A payment linked to this settlement no longer exists", false);
            return;
        }
        for (Payment payment : payments) {
            if (payment.getStatus() != PaymentStatus.CAPTURED) {
                fail(settlement, merchantBankAccount, "PAYMENT_NOT_CAPTURED",
                        "Payment " + payment.getId() + " is " + payment.getStatus(), false);
                return;
            }
        }

        LocalDateTime processedAt = LocalDateTime.now();
        settlement.setStatus(SettlementStatus.PROCESSED);
        settlement.setProcessedAt(processedAt);
        settlement.setMerchantBankAccount(merchantBankAccount);
        settlement.setBankReference(bankReference());
        settlement.setNextRetryAt(null);
        settlement.setFailureCode(null);
        settlement.setFailureReason(null);

        for (Payment payment : payments) {
            paymentTransitionService.createTransition(payment, PaymentEvent.SETTLE);
        }
        recordEvent(settlement, OutboxEventType.SETTLEMENT_PROCESSED, paymentIds);
        log.info("Settlement {} processed with bank reference {}", settlement.getId(), settlement.getBankReference());
    }

    private void fail(Settlement settlement, String merchantBankAccount, String code, String reason, boolean retryable) {
        int attempts = settlement.getRetryCount() + 1;
        settlement.setRetryCount(attempts);
        settlement.setFailureCode(code);
        settlement.setFailureReason(reason);
        if (!isBlank(merchantBankAccount)) {
            settlement.setMerchantBankAccount(merchantBankAccount);
        }

        if (retryable && attempts < settlementProperties.getMaxRetries()) {
            settlement.setNextRetryAt(LocalDateTime.now().plus(backoffFor(attempts)));
            log.info("Settlement {} payout failed ({}), retry {} at {}",
                    settlement.getId(), code, attempts, settlement.getNextRetryAt());
            return;
        }

        settlement.setStatus(SettlementStatus.FAILED);
        settlement.setFailedAt(LocalDateTime.now());
        settlement.setNextRetryAt(null);
        recordEvent(settlement, OutboxEventType.SETTLEMENT_FAILED, paymentIds(settlement));
        log.warn("Settlement {} marked FAILED ({}): {}", settlement.getId(), code, reason);
    }

    private List<UUID> paymentIds(Settlement settlement) {
        return settlementPaymentRepository.findByIdSettlementId(settlement.getId()).stream()
                .map(SettlementPayment::getId)
                .map(id -> id.getPaymentId())
                .toList();
    }

    private void recordEvent(Settlement settlement, OutboxEventType eventType, List<UUID> paymentIds) {
        outboxService.record(
                EventAggregateType.SETTLEMENT,
                settlement.getId(),
                eventType.name(),
                new SettlementEventPayload(
                        settlement.getId(),
                        settlement.getMerchantId(),
                        settlement.getGrossAmount(),
                        settlement.getFeeAmount(),
                        settlement.getGstAmount(),
                        settlement.getNetAmount(),
                        settlement.getStatus(),
                        settlement.getBankReference(),
                        settlement.getFailureCode(),
                        settlement.getFailureReason(),
                        settlement.getProcessedAt(),
                        paymentIds
                )
        );
    }

    private Duration backoffFor(int attempts) {
        List<Integer> schedule = settlementProperties.getBackoffSeconds();
        if (schedule == null || schedule.isEmpty()) {
            return Duration.ofMinutes(5);
        }
        int index = Math.min(attempts - 1, schedule.size() - 1);
        return Duration.ofSeconds(schedule.get(index));
    }

    private String bankReference() {
        return "STL" + RandomizerUtil.randomBase64(15);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

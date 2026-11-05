package com.vinayms.razorpayclone.operations.settlement;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import com.vinayms.razorpayclone.common.enums.SettlementStatus;
import com.vinayms.razorpayclone.operations.entity.Settlement;
import com.vinayms.razorpayclone.operations.entity.SettlementPayment;
import com.vinayms.razorpayclone.operations.entity.SettlementPaymentId;
import com.vinayms.razorpayclone.operations.repository.SettlementPaymentRepository;
import com.vinayms.razorpayclone.operations.repository.SettlementRepository;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final SettlementCalculator settlementCalculator;
    private final SettlementProperties settlementProperties;

    @Transactional
    public void createForMerchant(UUID merchantId, String currency, LocalDateTime cutoff) {
        List<Payment> payments = paymentRepository.findSettleablePayments(
                merchantId, currency, PaymentStatus.CAPTURED, cutoff);
        if (payments.isEmpty()) {
            return;
        }

        long gross = payments.stream().mapToLong(p -> p.getAmount().getAmountUnits()).sum();
        SettlementCalculator.SettlementAmounts amounts = settlementCalculator.calculate(
                (int) gross,
                0,
                settlementProperties.getFeePercent(),
                settlementProperties.getGstPercent());

        Settlement settlement = settlementRepository.save(Settlement.builder()
                .merchantId(merchantId)
                .grossAmount(new Money(amounts.grossUnits(), currency))
                .refundAmount(new Money(amounts.refundUnits(), currency))
                .feeAmount(new Money(amounts.feeUnits(), currency))
                .gstAmount(new Money(amounts.gstUnits(), currency))
                .netAmount(new Money(amounts.netUnits(), currency))
                .status(SettlementStatus.PENDING)
                .retryCount(0)
                .build());

        for (Payment payment : payments) {
            SettlementPayment link = new SettlementPayment();
            link.setId(new SettlementPaymentId(settlement.getId(), payment.getId()));
            link.setSettlement(settlement);
            settlementPaymentRepository.save(link);
        }
        log.info("Created pending settlement {} for merchant {} covering {} payments, net {}",
                settlement.getId(), merchantId, payments.size(), amounts.netUnits());
    }
}

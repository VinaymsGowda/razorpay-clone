package com.vinayms.razorpayclone.operations.settlement;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SettlementCalculator {

    public SettlementAmounts calculate(int grossUnits, int refundUnits, BigDecimal feePercent, BigDecimal gstPercent) {
        int feeUnits = percentOf(BigDecimal.valueOf(grossUnits), feePercent);
        int gstUnits = percentOf(BigDecimal.valueOf(feeUnits), gstPercent);
        int netUnits = grossUnits - feeUnits - gstUnits - refundUnits;
        return new SettlementAmounts(grossUnits, refundUnits, feeUnits, gstUnits, netUnits);
    }

    private int percentOf(BigDecimal base, BigDecimal percent) {
        return base.multiply(percent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public record SettlementAmounts(int grossUnits, int refundUnits, int feeUnits, int gstUnits, int netUnits) {
    }
}

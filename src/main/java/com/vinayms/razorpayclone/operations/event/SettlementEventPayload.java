package com.vinayms.razorpayclone.operations.event;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.SettlementStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SettlementEventPayload(
        UUID settlementId,
        UUID merchantId,
        Money grossAmount,
        Money feeAmount,
        Money gstAmount,
        Money netAmount,
        SettlementStatus status,
        String bankReference,
        String failureCode,
        String failureReason,
        LocalDateTime processedAt,
        List<UUID> paymentIds
) {
}

package com.vinayms.razorpayclone.operations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementPaymentId implements Serializable {

    @Column(name = "settlement_id", columnDefinition = "UUID")
    private UUID settlementId;

    @Column(name = "payment_id", columnDefinition = "UUID")
    private UUID paymentId;
}

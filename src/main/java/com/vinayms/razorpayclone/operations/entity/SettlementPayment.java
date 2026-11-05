package com.vinayms.razorpayclone.operations.entity;

import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "settlement_payment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_settlement_payment_payment_id",
                columnNames = "payment_id"
        )
)
public class SettlementPayment extends BaseAuditableEntity {

    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;
}

package com.vinayms.razorpayclone.operations.entity;

import com.vinayms.razorpayclone.common.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import com.vinayms.razorpayclone.common.entity.Money;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amountUnits", column = @Column(name = "gross_amount",nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "gross_currency",nullable = false))
    })
    private Money grossAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amountUnits", column = @Column(name = "refund_amount",nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "refund_currency",nullable = false))
    })
    private Money refundAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amountUnits", column = @Column(name = "fee_amount",nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "fee_currency",nullable = false))
    })
    private Money feeAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amountUnits", column = @Column(name = "gst_amount",nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "gst_currency",nullable = false))
    })
    private Money gstAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amountUnits", column = @Column(name = "net_amount",nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "net_currency",nullable = false))
    })
    private Money netAmount;

    @Column(name = "status")
    private SettlementStatus status;

    @Column(name = "bank_reference")
    private String bankReference;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}



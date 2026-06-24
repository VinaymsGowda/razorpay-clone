package com.vinayms.razorpayclone.payment.entity;

import com.vinayms.razorpayclone.common.entity.Money;
import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.common.enums.PaymentStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "method_details", columnDefinition = "jsonb")
    private String methodDetails;

    @Column(name = "bank_reference")
    private String bankReference;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_reason")
    private String errorReason;
}


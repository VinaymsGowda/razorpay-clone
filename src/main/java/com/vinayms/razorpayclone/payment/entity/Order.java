package com.vinayms.razorpayclone.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "notes", columnDefinition = "jsonb")
    private String notes;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}


package com.vinayms.razorpayclone.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.OrderStatus;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders",
indexes = {
        @Index(name = "idx_order_id_merchant_id", columnList = "id, merchant_id"),
        @Index(name = "idx_order_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_order_receipt", columnList = "receipt"),
        @Index(
                name = "idx_receipt_merchant_id",
                columnList = "merchant_id,receipt",
                unique = true
        )
}
)
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

    @Column(name = "receipt", nullable = false)
    private String receipt;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "attempts", nullable = false)
    @ColumnDefault(value = "0")
    private Integer attempts;

    @Column(name = "notes", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String,String> notes;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}


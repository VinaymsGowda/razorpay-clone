package com.vinayms.razorpayclone.merchant.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer",

indexes = {
        @Index(name = "idx_customer_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_customer_email", columnList = "email"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}


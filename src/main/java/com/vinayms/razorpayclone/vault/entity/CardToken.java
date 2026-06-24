package com.vinayms.razorpayclone.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardToken extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_card_id")
    private VaultCard vaultCard;

    private LocalDateTime revokedAt;
}


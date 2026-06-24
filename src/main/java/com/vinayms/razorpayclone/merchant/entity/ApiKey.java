package com.vinayms.razorpayclone.merchant.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import com.vinayms.razorpayclone.common.enums.Environment;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "key_id", nullable = false, unique = true)
    private String keyId;

    @Column(name = "key_secret_hash", nullable = false)
    private String keySecretHash;

    @Column(name = "webhook_secret_hash")
    private String webhookSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private Environment environment;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;

    @Column(name = "graceperiod_expires_at")
    private LocalDateTime gracePeriodExpiresAt;
}


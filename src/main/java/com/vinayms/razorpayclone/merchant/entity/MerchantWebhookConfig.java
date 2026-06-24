package com.vinayms.razorpayclone.merchant.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinayms.razorpayclone.common.entity.BaseAuditableEntity;
import java.util.UUID;

@Entity
@Table(name = "merchant_webhook_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantWebhookConfig extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "webhook_url", nullable = false)
    private String webhookUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "webhook_secret")
    private String webhookSecret;

}


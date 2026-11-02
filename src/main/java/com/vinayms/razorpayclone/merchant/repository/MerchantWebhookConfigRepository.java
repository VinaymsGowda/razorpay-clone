package com.vinayms.razorpayclone.merchant.repository;

import com.vinayms.razorpayclone.merchant.entity.MerchantWebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantWebhookConfigRepository extends JpaRepository<MerchantWebhookConfig, UUID> {
    List<MerchantWebhookConfig> findByMerchant_IdOrderByCreatedAtDesc(UUID merchantId);
    Optional<MerchantWebhookConfig> findByIdAndMerchant_Id(UUID id, UUID merchantId);
    List<MerchantWebhookConfig> findByMerchant_IdAndIsActiveTrue(UUID merchantId);
}

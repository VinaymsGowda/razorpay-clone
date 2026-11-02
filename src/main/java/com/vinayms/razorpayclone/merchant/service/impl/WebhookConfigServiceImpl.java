package com.vinayms.razorpayclone.merchant.service.impl;

import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.merchant.config.WebhookSecretEncryption;
import com.vinayms.razorpayclone.merchant.dto.request.WebhookConfigRequest;
import com.vinayms.razorpayclone.merchant.dto.response.WebhookConfigCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import com.vinayms.razorpayclone.merchant.entity.Merchant;
import com.vinayms.razorpayclone.merchant.entity.MerchantWebhookConfig;
import com.vinayms.razorpayclone.merchant.repository.MerchantRepository;
import com.vinayms.razorpayclone.merchant.repository.MerchantWebhookConfigRepository;
import com.vinayms.razorpayclone.merchant.service.WebhookConfigService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookConfigServiceImpl implements WebhookConfigService {
    private final MerchantWebhookConfigRepository webhookRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookSecretEncryption encryption;

    @Override
    @Transactional
    public WebhookConfigCreateResponse create(UUID merchantId, WebhookConfigRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant with id not found! " + merchantId));
        String secret = RandomizerUtil.randomBase64(32);
        MerchantWebhookConfig config = webhookRepository.save(MerchantWebhookConfig.builder()
                .merchant(merchant)
                .webhookUrl(request.targetUrl())
                .eventType(normalizeEvents(request.eventTypes()))
                .isActive(request.active())
                .webhookSecret(encryption.encrypt(secret))
                .build());
        return new WebhookConfigCreateResponse(toResponse(config), secret);
    }

    @Override
    @Transactional
    public List<WebhookConfigResponse> list(UUID merchantId) {
        return webhookRepository.findByMerchant_IdOrderByCreatedAtDesc(merchantId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public WebhookConfigResponse get(UUID merchantId, UUID id) {
        return toResponse(findOwned(merchantId, id));
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID id, WebhookConfigRequest request) {
        MerchantWebhookConfig config = findOwned(merchantId, id);
        config.setWebhookUrl(request.targetUrl());
        config.setEventType(normalizeEvents(request.eventTypes()));
        config.setIsActive(request.active());
        return toResponse(config);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID id) {
        webhookRepository.delete(findOwned(merchantId, id));
    }

    private MerchantWebhookConfig findOwned(UUID merchantId, UUID id) {
        return webhookRepository.findByIdAndMerchant_Id(id, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook config with id not found! " + id));
    }

    private WebhookConfigResponse toResponse(MerchantWebhookConfig config) {
        return new WebhookConfigResponse(config.getId(), config.getWebhookUrl(), config.getEventType(),
                config.getIsActive(), config.getCreatedAt(), config.getUpdatedAt());
    }

    private String normalizeEvents(String events) {
        return String.join(",", java.util.Arrays.stream(events.split(","))
                .map(String::trim).toList());
    }
}

package com.vinayms.razorpayclone.merchant.service;

import com.vinayms.razorpayclone.merchant.dto.request.WebhookConfigRequest;
import com.vinayms.razorpayclone.merchant.dto.response.WebhookConfigCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {
    WebhookConfigCreateResponse create(UUID merchantId, WebhookConfigRequest request);
    List<WebhookConfigResponse> list(UUID merchantId);
    WebhookConfigResponse get(UUID merchantId, UUID id);
    WebhookConfigResponse update(UUID merchantId, UUID id, WebhookConfigRequest request);
    void delete(UUID merchantId, UUID id);
}

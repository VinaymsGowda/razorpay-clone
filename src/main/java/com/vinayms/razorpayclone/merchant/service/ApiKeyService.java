package com.vinayms.razorpayclone.merchant.service;

import com.vinayms.razorpayclone.merchant.dto.request.ApiKeyRequest;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse createApiKey(UUID merchantId, @Valid ApiKeyRequest apiKeyRequest);

    List<ApiKeyResponse> getApiKeys(UUID merchant);

    void revokeKey(UUID merchantId, UUID id);

    ApiKeyCreateResponse rotateApiKey(UUID merchant, UUID id);

    List<ApiKeyResponse> listKeysByMerchant(UUID merchantId);
}
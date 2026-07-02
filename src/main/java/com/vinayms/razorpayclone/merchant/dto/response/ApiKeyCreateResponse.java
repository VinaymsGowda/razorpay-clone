package com.vinayms.razorpayclone.merchant.dto.response;

import com.vinayms.razorpayclone.common.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(UUID id, String keyId, String keySecret, Environment environment) {
}

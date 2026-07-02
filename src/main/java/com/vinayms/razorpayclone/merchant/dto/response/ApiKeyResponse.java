package com.vinayms.razorpayclone.merchant.dto.response;

import com.vinayms.razorpayclone.common.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String keyId,
        Environment environment,
        Boolean enabled,
        LocalDateTime lastUsedAt,
        LocalDateTime rotatedAt,
        LocalDateTime gracePeriodExpiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }

package com.vinayms.razorpayclone.common.cacheEntryDTOs;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.vinayms.razorpayclone.common.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyCache {

    private UUID id;
    private UUID merchantId;
    private String keyId;
    private String keySecretHash;
    private String webhookSecretHash;
    private Environment environment;
    private Boolean enabled;
    private LocalDateTime lastUsedAt;
    private LocalDateTime rotatedAt;
    private LocalDateTime gracePeriodExpiresAt;
    private String prevKeySecretHash;
}

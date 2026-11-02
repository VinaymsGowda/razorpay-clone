package com.vinayms.razorpayclone.merchant.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record WebhookConfigResponse(
        UUID id,
        String targetUrl,
        String eventTypes,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }

package com.vinayms.razorpayclone.merchant.dto.request;

import com.vinayms.razorpayclone.merchant.validation.ValidWebhookEvents;
import com.vinayms.razorpayclone.merchant.validation.ValidWebhookUrl;
import jakarta.validation.constraints.NotNull;

public record WebhookConfigRequest(
        @ValidWebhookUrl String targetUrl,
        @ValidWebhookEvents String eventTypes,
        @NotNull Boolean active
) { }

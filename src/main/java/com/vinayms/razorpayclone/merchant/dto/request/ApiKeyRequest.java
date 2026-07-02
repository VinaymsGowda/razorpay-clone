package com.vinayms.razorpayclone.merchant.dto.request;

import com.vinayms.razorpayclone.common.enums.Environment;
import jakarta.validation.constraints.NotNull;

public record ApiKeyRequest(@NotNull Environment environment) {
}

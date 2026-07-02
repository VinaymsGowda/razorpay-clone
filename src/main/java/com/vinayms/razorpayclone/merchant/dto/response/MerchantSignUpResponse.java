package com.vinayms.razorpayclone.merchant.dto.response;

import com.vinayms.razorpayclone.common.enums.BusinessType;
import com.vinayms.razorpayclone.common.enums.MerchantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MerchantSignUpResponse(


        UUID id,

        String name,


        String email,


        BusinessType businessType,

        String businessName,

        MerchantStatus merchantStatus
) {
}

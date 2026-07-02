package com.vinayms.razorpayclone.merchant.dto.request;

import com.vinayms.razorpayclone.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record  MerchantSignUpRequest(

    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email,please provide a valid email")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8,message = "Password must be at least 8 characters")
    String password,


    BusinessType businessType,

    @NotNull(message = "Business name is required")
    String businessName
){

}

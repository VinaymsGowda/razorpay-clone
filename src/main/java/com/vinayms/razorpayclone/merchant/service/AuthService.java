package com.vinayms.razorpayclone.merchant.service;

import com.vinayms.razorpayclone.merchant.dto.request.LoginRequest;
import com.vinayms.razorpayclone.merchant.dto.request.MerchantSignUpRequest;
import com.vinayms.razorpayclone.merchant.dto.response.LoginResponse;
import com.vinayms.razorpayclone.merchant.dto.response.MerchantSignUpResponse;
import jakarta.validation.Valid;

public interface AuthService {

    MerchantSignUpResponse signUp(MerchantSignUpRequest newMerchant);

    LoginResponse login(LoginRequest newMerchant);
}
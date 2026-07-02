package com.vinayms.razorpayclone.merchant.service;

import com.vinayms.razorpayclone.merchant.dto.request.MerchantSignUpRequest;
import com.vinayms.razorpayclone.merchant.dto.response.MerchantSignUpResponse;

public interface AuthService {

    MerchantSignUpResponse signUp(MerchantSignUpRequest newMerchant);
}
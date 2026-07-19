package com.vinayms.razorpayclone.vault.service;


import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.vault.dto.request.TokenizeReq;
import com.vinayms.razorpayclone.vault.dto.response.TokenizeResp;

import java.util.Map;
import java.util.UUID;


public interface VaultService {

    public TokenizeResp  tokenize(TokenizeReq req, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails, PaymentMethod paymentMethod);
}

package com.vinayms.razorpayclone.vault.controller;

import com.vinayms.razorpayclone.merchant.security.MerchantContext;
import com.vinayms.razorpayclone.vault.dto.request.TokenizeReq;
import com.vinayms.razorpayclone.vault.dto.response.TokenizeResp;
import com.vinayms.razorpayclone.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;
    private final MerchantContext merchantContext;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResp> tokenize(@RequestBody @Valid TokenizeReq req) {
        TokenizeResp token=vaultService.tokenize(req, merchantContext.getMerchantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }
}

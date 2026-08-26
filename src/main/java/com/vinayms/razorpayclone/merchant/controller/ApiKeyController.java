package com.vinayms.razorpayclone.merchant.controller;

import com.vinayms.razorpayclone.merchant.dto.request.ApiKeyRequest;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.vinayms.razorpayclone.merchant.security.MerchantContext;
import com.vinayms.razorpayclone.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping(path = "/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping(path = "/")
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@RequestBody @Valid ApiKeyRequest apiKeyRequest) {
        UUID merchant=merchantContext.getMerchantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createApiKey(merchant, apiKeyRequest));

    }


    @GetMapping(path="/")
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys() {
        // Implementation for retrieving API keys
        UUID merchant=merchantContext.getMerchantId();
        return ResponseEntity.ok(apiKeyService.getApiKeys(merchant));
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable UUID id) {
        // Implementation for deleting API key
        UUID merchant=merchantContext.getMerchantId();

        apiKeyService.revokeKey(merchant,id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}")
    
    public ResponseEntity<ApiKeyCreateResponse> rotateApiKey(@PathVariable UUID id) {
        UUID merchant=merchantContext.getMerchantId();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.rotateApiKey(merchant, id));
    }

}

package com.vinayms.razorpayclone.merchant.controller;

import com.vinayms.razorpayclone.merchant.dto.request.ApiKeyRequest;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.vinayms.razorpayclone.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/merchants/{merchant}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping(path = "/")
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@PathVariable UUID merchant, @RequestBody @Valid ApiKeyRequest apiKeyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createApiKey(merchant, apiKeyRequest));

    }


    @GetMapping(path="/")
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys(@PathVariable UUID merchant) {
        // Implementation for retrieving API keys
        return ResponseEntity.ok(apiKeyService.getApiKeys(merchant));
    }

    @DeleteMapping(path="/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable UUID merchant, @PathVariable UUID id) {
        // Implementation for deleting API key

        apiKeyService.revokeKey(merchant,id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}")
    
    public ResponseEntity<ApiKeyCreateResponse> rotateApiKey(@PathVariable UUID merchant, @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.rotateApiKey(merchant, id));
    }

}

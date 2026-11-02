package com.vinayms.razorpayclone.merchant.controller;

import com.vinayms.razorpayclone.merchant.dto.request.WebhookConfigRequest;
import com.vinayms.razorpayclone.merchant.dto.response.WebhookConfigCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import com.vinayms.razorpayclone.merchant.security.MerchantContext;
import com.vinayms.razorpayclone.merchant.service.WebhookConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {
    private final WebhookConfigService webhookConfigService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<WebhookConfigCreateResponse> create(@RequestBody @Valid WebhookConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(webhookConfigService.create(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public List<WebhookConfigResponse> list() {
        return webhookConfigService.list(merchantContext.getMerchantId());
    }

    @GetMapping("/{id}")
    public WebhookConfigResponse get(@PathVariable UUID id) {
        return webhookConfigService.get(merchantContext.getMerchantId(), id);
    }

    @PutMapping("/{id}")
    public WebhookConfigResponse update(@PathVariable UUID id, @RequestBody @Valid WebhookConfigRequest request) {
        return webhookConfigService.update(merchantContext.getMerchantId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        webhookConfigService.delete(merchantContext.getMerchantId(), id);
        return ResponseEntity.noContent().build();
    }
}

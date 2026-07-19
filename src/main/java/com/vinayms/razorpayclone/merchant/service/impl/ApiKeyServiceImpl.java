package com.vinayms.razorpayclone.merchant.service.impl;

import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.merchant.dto.request.ApiKeyRequest;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyCreateResponse;
import com.vinayms.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.vinayms.razorpayclone.merchant.entity.ApiKey;
import com.vinayms.razorpayclone.merchant.entity.Merchant;
import com.vinayms.razorpayclone.merchant.mapper.ApiKeyMapper;
import com.vinayms.razorpayclone.merchant.repository.ApiKeyRepository;
import com.vinayms.razorpayclone.merchant.repository.MerchantRepository;
import com.vinayms.razorpayclone.merchant.service.ApiKeyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;

    @Transactional
    @Override
    public ApiKeyCreateResponse createApiKey(UUID merchantId, ApiKeyRequest apiKeyRequest) {
        Merchant merchant=merchantRepository.findById(merchantId)
                .orElseThrow(()->new ResourceNotFoundException("Merchant with id not found!"+merchantId));
        // Implementation for creating API key

        String randomString= RandomizerUtil.randomBase64(24);
        String keyId="rzp_"+apiKeyRequest.environment().name().toLowerCase()+"_"+randomString;

        String secret=RandomizerUtil.randomBase64(32);


        ApiKey apiKey=ApiKey.builder()
                .keyId(keyId)
                .keySecretHash(secret)
                .environment(apiKeyRequest.environment())
                .merchant(merchant)
                .enabled(true)
                .build();
        ApiKey newApiKey=apiKeyRepository.save(apiKey);
        return new ApiKeyCreateResponse(
                newApiKey.getId(),
                newApiKey.getKeyId(),
                newApiKey.getKeySecretHash(),
                newApiKey.getEnvironment()
        );
    }

    @Override
    public List<ApiKeyResponse> getApiKeys(UUID merchantId) {
        Merchant merchant=merchantRepository.findById(merchantId)
                .orElseThrow(()->new ResourceNotFoundException("Merchant with id not found! "+merchantId));
        var apiKeys=apiKeyRepository.findByMerchant_Id(merchantId);

        return apiKeyMapper.toApiKeyResponseList(apiKeys);
    }

    @Override
    @Transactional
    public void revokeKey(UUID merchantId, UUID id) {
        ApiKey apiKey=apiKeyRepository.findByIdAndMerchant_Id(id,merchantId).
                orElseThrow(()->new ResourceNotFoundException("ApiKey with id not found! "+id));



        if(!apiKey.getEnabled()){
            throw new ResourceNotFoundException("ApiKey with id not found! "+id);
        }

        apiKey.setEnabled(false);
    }

    @Transactional
    @Override
    public ApiKeyCreateResponse rotateApiKey(UUID merchant, UUID id) {

        ApiKey apiKey=apiKeyRepository.findByIdAndMerchant_Id(id,merchant).
                orElseThrow(()->new ResourceNotFoundException("ApiKey with id not found! "+id));


        String oldSecretKeyHash=apiKey.getKeySecretHash();
        // TODO: Hash it with bcrypt encoder
        String newSecretKeyHash=RandomizerUtil.randomBase64(32);

        apiKey.setPrevKeySecretHash(oldSecretKeyHash);
        apiKey.setKeySecretHash(newSecretKeyHash);
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        return new ApiKeyCreateResponse(
                apiKey.getId(),
                apiKey.getKeyId(),
                newSecretKeyHash,
                apiKey.getEnvironment()
        );



    }


    @Override
    public List<ApiKeyResponse> listKeysByMerchant(UUID merchantId){
        List<ApiKey> apiKeys=apiKeyRepository.findByMerchant_Id(merchantId);
        return apiKeyMapper.toApiKeyResponseList(apiKeys);
    }



}

package com.vinayms.razorpayclone.merchant.repository;

import com.vinayms.razorpayclone.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    public List<ApiKey> findByMerchant_Id(UUID merchantId);

    public Optional<ApiKey> findByIdAndMerchant_Id(UUID id,UUID merchantId);

    Optional<ApiKey> findByKeyId(String keyId);
}
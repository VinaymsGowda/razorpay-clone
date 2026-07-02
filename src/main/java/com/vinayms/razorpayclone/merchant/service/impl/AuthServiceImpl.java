
 package com.vinayms.razorpayclone.merchant.service.impl;

import com.vinayms.razorpayclone.common.enums.MerchantStatus;
import com.vinayms.razorpayclone.common.enums.UserRole;
import com.vinayms.razorpayclone.common.exceptions.ConflictException;
import com.vinayms.razorpayclone.merchant.dto.request.MerchantSignUpRequest;
import com.vinayms.razorpayclone.merchant.dto.response.MerchantSignUpResponse;
import com.vinayms.razorpayclone.merchant.entity.AppUser;
import com.vinayms.razorpayclone.merchant.entity.Merchant;
import com.vinayms.razorpayclone.merchant.repository.AppUserRepository;
import com.vinayms.razorpayclone.merchant.repository.MerchantRepository;
import com.vinayms.razorpayclone.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

 private final AppUserRepository appUserRepository;
 private final MerchantRepository merchantRepository;

 @Transactional
 @Override
 public MerchantSignUpResponse signUp(MerchantSignUpRequest newMerchant) {

  boolean existingMerchant=merchantRepository.existsByEmail(newMerchant.email());

  if(existingMerchant){
   throw new ConflictException("Merchant with email already exists!"+newMerchant.email());
  }
  boolean appUserExists=appUserRepository.existsByEmail(newMerchant.email());

  if(appUserExists){
   throw new ConflictException("AppUser with email already exists!"+newMerchant.email());
  }


  Merchant merchant=Merchant.builder()
          .name(newMerchant.name())
          .email(newMerchant.email())
          .businessName(newMerchant.businessName())
          .businessType(newMerchant.businessType())
          .status(MerchantStatus.PENDING_KYC)
          .build();

  merchant=merchantRepository.save(merchant);

  AppUser newUser=AppUser.builder()
          .email(newMerchant.email())
          .password(newMerchant.password())
          .role(UserRole.OWNER)
          .merchant(merchant)
          .build();

  appUserRepository.save(newUser);

  return new MerchantSignUpResponse(
          merchant.getId(),
          merchant.getName(),
          merchant.getEmail(),
          merchant.getBusinessType(),
          merchant.getBusinessName(),
          merchant.getStatus()
  );
 }
}
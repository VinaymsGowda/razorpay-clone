
 package com.vinayms.razorpayclone.merchant.service.impl;

import com.vinayms.razorpayclone.common.enums.MerchantStatus;
import com.vinayms.razorpayclone.common.enums.UserRole;
import com.vinayms.razorpayclone.common.exceptions.ConflictException;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.merchant.dto.request.LoginRequest;
import com.vinayms.razorpayclone.merchant.dto.request.MerchantSignUpRequest;
import com.vinayms.razorpayclone.merchant.dto.response.LoginResponse;
import com.vinayms.razorpayclone.merchant.dto.response.MerchantSignUpResponse;
import com.vinayms.razorpayclone.merchant.entity.AppUser;
import com.vinayms.razorpayclone.merchant.entity.Merchant;
import com.vinayms.razorpayclone.merchant.mapper.MerchantMapper;
import com.vinayms.razorpayclone.merchant.repository.AppUserRepository;
import com.vinayms.razorpayclone.merchant.repository.MerchantRepository;
import com.vinayms.razorpayclone.merchant.security.JwtUtil;
import com.vinayms.razorpayclone.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

 private final AppUserRepository appUserRepository;
 private final MerchantRepository merchantRepository;
 private final MerchantMapper merchantMapper;
 private final PasswordEncoder passwordEncoder;
 private final AuthenticationManager authenticationManager;
 private final JwtUtil jwtUtil;

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


  Merchant merchant=merchantMapper.toMerchant(newMerchant);
  merchant.setStatus(MerchantStatus.PENDING_KYC);
  merchant=merchantRepository.save(merchant);

  String password=passwordEncoder.encode(newMerchant.password());
  AppUser newUser=AppUser.builder()
          .email(newMerchant.email())
          .password(password)
          .role(UserRole.OWNER)
          .merchant(merchant)
          .build();

  appUserRepository.save(newUser);

  return merchantMapper.toMerchantSignUpResponse(merchant);
 }

 @Override
 public LoginResponse login(LoginRequest userDetails) {


    AppUser user=appUserRepository.findByEmail(userDetails.email()).orElseThrow(
            ()->new ResourceNotFoundException("User with email not found")
    );
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userDetails.email(),userDetails.password())
    );

    String accessToken= jwtUtil.generateAccessToken(
            user.getEmail(),
            user.getMerchant().getId(),
            user.getRole().name()
    );
    return new LoginResponse(accessToken);
 }
}
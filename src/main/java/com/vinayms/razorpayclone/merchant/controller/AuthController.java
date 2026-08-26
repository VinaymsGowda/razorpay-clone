package com.vinayms.razorpayclone.merchant.controller;

import com.vinayms.razorpayclone.merchant.dto.request.LoginRequest;
import com.vinayms.razorpayclone.merchant.dto.request.MerchantSignUpRequest;
import com.vinayms.razorpayclone.merchant.dto.response.LoginResponse;
import com.vinayms.razorpayclone.merchant.dto.response.MerchantSignUpResponse;
import com.vinayms.razorpayclone.merchant.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MerchantSignUpResponse> handleSignUp(@RequestBody @Valid MerchantSignUpRequest newMerchant){
        MerchantSignUpResponse signUpResponse = authService.signUp(newMerchant);
        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> handleLogin(@RequestBody @Valid LoginRequest newMerchant){
        LoginResponse loginResponse = authService.login(newMerchant);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }
}

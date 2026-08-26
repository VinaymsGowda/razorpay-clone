package com.vinayms.razorpayclone.merchant.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class JwtUtil {

    @Value("${jwt.secret-key}")
    public String secretKey;


    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, UUID merchantId,String role){
        SecretKey secretKey = getSecretKey();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("merchantId", merchantId);
        claims.put("role", role);
        Instant currentTime=Instant.now();
        Date now =Date.from(currentTime);
        Date expiration = Date.from(currentTime.plusSeconds(60*60));
        return Jwts.builder()
                .claims(claims)
                .id(merchantId.toString())
                .subject(email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }


    public Claims validateAccessToken(String token){
        SecretKey secretKey = getSecretKey();

        return Jwts.parser().
                verifyWith(secretKey).
                build().
                parseSignedClaims(token)
                .getPayload();
    }

    public String extractRoleFromClaims(Claims claims){
        return claims.get("role").toString();
    }

    public UUID extraMerchantIdFromClaims(Claims claims) {
        return UUID.fromString(claims.get("merchantId").toString());
    }
}

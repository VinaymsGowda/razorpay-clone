package com.vinayms.razorpayclone.merchant.security;

import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.common.exceptions.RateLimitExceededException;
import com.vinayms.razorpayclone.common.cacheEntryDTOs.ApiKeyCache;
import com.vinayms.razorpayclone.common.ratelimiting.SlidingWindowRateLimiter;
import com.vinayms.razorpayclone.common.services.RedisService;
import com.vinayms.razorpayclone.merchant.entity.ApiKey;
import com.vinayms.razorpayclone.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {


    private final ApiKeyRepository apiKeyRepository;
    private static final String BASIC="Basic ";
    private final PasswordEncoder passwordEncoder;
    private final MerchantContext merchantContext;
    private final RedisService redisService;
    private final SlidingWindowRateLimiter rateLimiter;


    private final HandlerExceptionResolver exceptionResolver;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository,
                            PasswordEncoder passwordEncoder,
                            MerchantContext merchantContext,
                            RedisService redisService,
                            SlidingWindowRateLimiter rateLimiter,
                            @Qualifier("handlerExceptionResolver")
                            HandlerExceptionResolver exceptionResolver) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.merchantContext = merchantContext;
        this.redisService = redisService;
        this.rateLimiter = rateLimiter;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        try {


        log.info("Incoming request received {}", request.getRequestURI());

        String authHeader=request.getHeader("authorization");

        if(authHeader==null || !authHeader.startsWith(BASIC)){
            filterChain.doFilter(request,response);
            return;
        }

        String[] decode=decode(authHeader);
        if(decode==null){
            filterChain.doFilter(request,response);
            return;
        }
        String keyId=decode[0];
        String keySecret=decode[1];

        String cacheKey="apiKey:"+keyId;

        Optional<ApiKeyCache> cachedData=redisService.get(cacheKey,ApiKeyCache.class);


        ApiKeyCache apiKey;

        if(cachedData.isEmpty()){
            ApiKey persistedApiKey = apiKeyRepository.findByKeyId(keyId).orElseThrow(
                    () -> new ResourceNotFoundException("Key id not found")
            );
            apiKey = ApiKeyCache.builder()
                    .id(persistedApiKey.getId())
                    .merchantId(persistedApiKey.getMerchant().getId())
                    .keyId(persistedApiKey.getKeyId())
                    .keySecretHash(persistedApiKey.getKeySecretHash())
                    .webhookSecretHash(persistedApiKey.getWebhookSecretHash())
                    .environment(persistedApiKey.getEnvironment())
                    .enabled(persistedApiKey.getEnabled())
                    .lastUsedAt(persistedApiKey.getLastUsedAt())
                    .rotatedAt(persistedApiKey.getRotatedAt())
                    .gracePeriodExpiresAt(persistedApiKey.getGracePeriodExpiresAt())
                    .prevKeySecretHash(persistedApiKey.getPrevKeySecretHash())
                    .build();
            redisService.set(cacheKey,apiKey,300);
        }
        else{
            apiKey=cachedData.get();
        }
        // verify keysecret against db hash

        if(Boolean.FALSE.equals(apiKey.getEnabled()) || !isApiKeyValid(apiKey,keySecret)){
            throw new BadRequestException("Invalid API Key credentials");
        }

        SlidingWindowRateLimiter.RateLimitResult rateLimitResult = rateLimiter.checkRequest(keyId);
        if (!rateLimitResult.allowed()) {
            response.setHeader("X-Allowed-Request-Per-Minute",
                    String.valueOf(rateLimitResult.allowedRequestsPerMinute()));
            response.setHeader("X-Current-Request-Per-Minute",
                    String.valueOf(rateLimitResult.currentRequestsPerMinute()));
            throw new RateLimitExceededException(
                    "You can make up to " + rateLimitResult.allowedRequestsPerMinute()
                            + " requests per minute. Please try again after some time.");
        }

        var auth=new UsernamePasswordAuthenticationToken(
                keyId, null,
                List.of(
                        new SimpleGrantedAuthority(
                                "API_KEY_ROLE")
                )
        );
        merchantContext.setMerchantId(apiKey.getMerchantId());
        merchantContext.setKeyId(keyId);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request,response);
        }catch (Exception e){
            exceptionResolver.resolveException(request,response,null,e);
        }
    }

    public boolean isApiKeyValid(ApiKeyCache apiKey,String keySecret){
        if(passwordEncoder.matches(keySecret,apiKey.getKeySecretHash())){
            return true;
        }
        if(apiKey.getPrevKeySecretHash()!=null && passwordEncoder.matches(keySecret,apiKey.getPrevKeySecretHash()) &&
                apiKey.getGracePeriodExpiresAt()!=null
                && LocalDateTime.now().isBefore(apiKey.getGracePeriodExpiresAt())
        ){
            return true;
        }
        return false;



    }

    private String[] decode(String header){
        String encodedHeader=header.substring(BASIC.length());

        String decoded=new String(Base64.getDecoder().decode(encodedHeader));

        int colon=decoded.indexOf(':');
        if(colon<1){
            return null;
        }

        return new String[]{decoded.substring(0, colon),decoded.substring(colon+1)};
    }
}

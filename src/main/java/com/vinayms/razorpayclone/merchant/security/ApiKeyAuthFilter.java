package com.vinayms.razorpayclone.merchant.security;

import com.vinayms.razorpayclone.common.exceptions.BadRequestException;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.merchant.entity.ApiKey;
import com.vinayms.razorpayclone.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@Component
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {


    private final ApiKeyRepository apiKeyRepository;
    private static final String BASIC="Basic ";
    private final PasswordEncoder passwordEncoder;
    private final MerchantContext merchantContext;

    private final HandlerExceptionResolver exceptionResolver;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository,
                            PasswordEncoder passwordEncoder,
                            MerchantContext merchantContext,
                            @Qualifier("handlerExceptionResolver")
                            HandlerExceptionResolver exceptionResolver) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.merchantContext = merchantContext;
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


        ApiKey apiKey=apiKeyRepository.findByKeyId(keyId).orElseThrow(
                ()->new ResourceNotFoundException("Key id not found")
        );

        // verify keysecret against db hash

        if(apiKey.getEnabled()==false || !isApiKeyValid(apiKey,keySecret)){
            throw new BadRequestException("Invalid API Key credentials");
        }

        var auth=new UsernamePasswordAuthenticationToken(
                keyId, null,
                List.of(
                        new SimpleGrantedAuthority(
                                "API_KEY_ROLE")
                )
        );
        merchantContext.setMerchantId(apiKey.getMerchant().getId());
        merchantContext.setKeyId(keyId);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request,response);
        }catch (Exception e){
            exceptionResolver.resolveException(request,response,null,e);
        }
    }

    public boolean isApiKeyValid(ApiKey apiKey,String keySecret){
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

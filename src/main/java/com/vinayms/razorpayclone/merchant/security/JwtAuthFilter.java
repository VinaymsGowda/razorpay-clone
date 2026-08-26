package com.vinayms.razorpayclone.merchant.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MerchantContext merchantContext;


    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthFilter(JwtUtil jwtUtil,
                         @Qualifier("handlerExceptionResolver")
                         HandlerExceptionResolver exceptionResolver,
                         MerchantContext merchantContext
                         ) {
        this.jwtUtil = jwtUtil;
        this.exceptionResolver = exceptionResolver;
        this.merchantContext = merchantContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain){

        log.info("Incoming request : {} ",request.getRequestURI());
        try {
        String authHeader=request.getHeader("Authorization");

        if(authHeader==null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request,response);
            return;
        }
        String token=authHeader.split(" ")[1];

        Claims claims=jwtUtil.validateAccessToken(token);

        if(claims==null){
            filterChain.doFilter(request,response);
            return;
        }

        if(SecurityContextHolder.getContext().getAuthentication()==null){
            var auth=new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            jwtUtil.extractRoleFromClaims(claims))
                            )
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            merchantContext.setMerchantId(jwtUtil.extraMerchantIdFromClaims(claims));
        }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            exceptionResolver.resolveException(request,response,null,e);
        }


    }
}

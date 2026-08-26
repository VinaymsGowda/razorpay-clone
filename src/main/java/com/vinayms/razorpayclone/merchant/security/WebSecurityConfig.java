package com.vinayms.razorpayclone.merchant.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.http.HttpRequest;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ApiKeyAuthFilter apiKeyAuthFilter;



    private static final String[] JWT_ROUTES={"/v1/auth/**","/v1/merchants/**"};
    private static final String[] API_KEY_ROUTES={"/v1/order/**","/v1/payments/**"};

    @Bean
    public SecurityFilterChain jwtFilter(HttpSecurity security){

        return security
                .securityMatcher(JWT_ROUTES)
                .csrf(csrf->csrf.disable())
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        jwtAuthFilter,UsernamePasswordAuthenticationFilter.class
                )
                .authorizeHttpRequests(
                    requests->
                            requests.
                            requestMatchers("/v1/auth/login","/v1/auth/signup").permitAll()
                                    .anyRequest().authenticated()

                ).build();
    }

    @Bean
    public SecurityFilterChain apiKeyFilter(HttpSecurity security){

        return security
                .securityMatcher(API_KEY_ROUTES)
                .csrf(csrf->csrf.disable())
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        apiKeyAuthFilter,UsernamePasswordAuthenticationFilter.class
                )
                .authorizeHttpRequests(
                        requests->
                                requests.anyRequest().authenticated()

                ).build();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            MerchantUserDetails merchantUserDetails,
            PasswordEncoder passwordEncoder
    ){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(
                merchantUserDetails
        );
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}

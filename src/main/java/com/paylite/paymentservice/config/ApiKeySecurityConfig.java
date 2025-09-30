package com.paylite.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class ApiKeySecurityConfig {

    @Value("${app.security.api-keys.test-api-key-123}")
    private String testClient;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/payments/**").authenticated()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/webhooks/psp").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new ApiKeyAuthFilter(getApiKeysMap()), BasicAuthenticationFilter.class);

        return http.build();
    }

    private Map<String, String> getApiKeysMap() {
        Map<String, String> apiKeys = new HashMap<>();
        apiKeys.put("test-api-key-123", testClient);
        return apiKeys;
    }
}
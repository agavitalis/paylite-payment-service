package com.paylite.paymentservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
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

    @RequiredArgsConstructor
    public static class ApiKeyAuthFilter extends OncePerRequestFilter {

        private final Map<String, String> apiKeys;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String apiKey = request.getHeader("X-API-Key");

            if (apiKey == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing API Key");
                return;
            }

            if (apiKeys.containsKey(apiKey)) {
                String clientName = apiKeys.get(apiKey);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        clientName, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API Key");
                return;
            }

            filterChain.doFilter(request, response);
        }
    }
}
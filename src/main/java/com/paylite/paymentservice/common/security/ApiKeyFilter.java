package com.paylite.paymentservice.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paylite.paymentservice.common.exceptions.ErrorDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public class ApiKeyFilter extends OncePerRequestFilter {

    private final Map<String, String> apiKeys;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(Map<String, String> apiKeys) {
        this.apiKeys = apiKeys;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip API key check for public endpoints
        String path = request.getServletPath();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing API Key", request.getRequestURI());
            return;
        }

        // Check if the API key exists in our map
        if (apiKeys.containsKey(apiKey)) {
            String clientName = apiKeys.get(apiKey);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    clientName,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid API Key", request.getRequestURI());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");


        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setTimestamp(LocalDateTime.now());
        errorDetails.setMessage(message);
        errorDetails.setDetails(path);
        errorDetails.setValidationErrors(Collections.emptyMap());

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/") ||
                path.startsWith("/error") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/api/v1/webhooks/psp") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars");
    }
}
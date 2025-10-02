package com.paylite.paymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paylite.paymentservice.common.security.ApiKeyFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyFilterTests {

    private ApiKeyFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    private ObjectMapper mapper;

    private final Map<String, String> apiKeys = Map.of(
            "valid-key", "test-client"
    );

    @BeforeEach
    void setUp() {
        filter = new ApiKeyFilter(apiKeys);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        // Ensure clean security context
        SecurityContextHolder.clearContext();

        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingApiKey_returns401AndErrorDetails() throws Exception {
        request.setRequestURI("/api/v1/payments");
        request.setServletPath("/api/v1/payments");

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        // parse the response body into a Map to avoid LocalDateTime deserialization
        Map<String, Object> body = mapper.readValue(response.getContentAsString(), Map.class);

        assertEquals("Missing API Key", body.get("message"));
        assertEquals("/api/v1/payments", body.get("details"));
        assertNotNull(body.get("validationErrors"));
    }

    @Test
    void invalidApiKey_returns401AndErrorDetails() throws Exception {
        request.addHeader("X-API-Key", "wrong-key");
        request.setRequestURI("/api/v1/payments");
        request.setServletPath("/api/v1/payments");

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Map<String, Object> body = mapper.readValue(response.getContentAsString(), Map.class);

        assertEquals("Invalid API Key", body.get("message"));
        // optional: assert details path as well
        assertEquals("/api/v1/payments", body.get("details"));
    }

    @Test
    void validApiKey_setsSecurityContextAndInvokesChain() throws Exception {
        request.addHeader("X-API-Key", "valid-key");
        request.setRequestURI("/api/v1/payments");
        request.setServletPath("/api/v1/payments");

        filter.doFilterInternal(request, response, chain);

        // since valid, response should not be 401
        assertNotEquals(401, response.getStatus());

        // SecurityContext should have an authentication principal of the client name
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("test-client", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
    }

    @Test
    void publicEndpoint_skipsAuthentication() throws Exception {
        // public endpoint list includes /api/v1/webhooks/psp
        request.setRequestURI("/api/v1/webhooks/psp/some");
        request.setServletPath("/api/v1/webhooks/psp/some");

        filter.doFilterInternal(request, response, chain);

        // Should forward without requiring API key
        assertNotEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

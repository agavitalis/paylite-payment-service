package com.paylite.paymentservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.common.exceptions.PayliteException;
import com.paylite.paymentservice.common.utilities.HashUtility;
import com.paylite.paymentservice.modules.payment.entity.IdempotencyKey;
import com.paylite.paymentservice.modules.payment.repository.IdempotencyKeyRepository;
import com.paylite.paymentservice.modules.payment.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTests {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HashUtility hashUtility;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Captor
    private ArgumentCaptor<IdempotencyKey> keyCaptor;

    @BeforeEach
    void setUp() {
        // Mockito injects mocks
    }

    @Test
    void generateRequestHash_success_returnsHash() throws Exception {
        Object request = new Object();
        String json = "{\"foo\":\"bar\"}";
        String expectedHash = "hash-value";

        when(objectMapper.writeValueAsString(request)).thenReturn(json);
        when(hashUtility.generateSha256Hash(json)).thenReturn(expectedHash);

        String result = idempotencyService.generateRequestHash(request);

        assertEquals(expectedHash, result);
        verify(objectMapper).writeValueAsString(request);
        verify(hashUtility).generateSha256Hash(json);
    }

    @Test
    void getCachedResponse_present_returnsOptional() {
        String key = "idem-1";
        IdempotencyKey entity = new IdempotencyKey();
        entity.setKey(key);
        entity.setResponseBody("cached-response");

        when(idempotencyKeyRepository.findByKey(key)).thenReturn(Optional.of(entity));

        Optional<String> result = idempotencyService.getCachedResponse(key);

        assertTrue(result.isPresent());
        assertEquals("cached-response", result.get());
        verify(idempotencyKeyRepository).findByKey(key);
    }

    @Test
    void getCachedResponse_absent_returnsEmptyOptional() {
        String key = "idem-none";
        when(idempotencyKeyRepository.findByKey(key)).thenReturn(Optional.empty());

        Optional<String> result = idempotencyService.getCachedResponse(key);

        assertFalse(result.isPresent());
        verify(idempotencyKeyRepository).findByKey(key);
    }

    @Test
    void storeIdempotencyKey_savesEntity() {
        String idKey = "idem-store";
        String requestHash = "req-hash";
        String response = "{\"ok\":true}";

        // No need to stub save; just verify it's called with correct values
        idempotencyService.storeIdempotencyKey(idKey, requestHash, response);

        verify(idempotencyKeyRepository).save(keyCaptor.capture());
        IdempotencyKey captured = keyCaptor.getValue();
        assertEquals(idKey, captured.getKey());
        assertEquals(requestHash, captured.getRequestHash());
        assertEquals(response, captured.getResponseBody());
    }

    @Test
    void isDuplicateRequest_trueWhenExistsDifferentHash() {
        String key = "idem-dup";
        String hash = "h1";
        when(idempotencyKeyRepository.existsByKeyAndRequestHashNot(key, hash)).thenReturn(true);

        boolean result = idempotencyService.isDuplicateRequest(key, hash);

        assertTrue(result);
        verify(idempotencyKeyRepository).existsByKeyAndRequestHashNot(key, hash);
    }

    @Test
    void isDuplicateRequest_falseWhenNotExistsDifferentHash() {
        String key = "idem-notdup";
        String hash = "h2";
        when(idempotencyKeyRepository.existsByKeyAndRequestHashNot(key, hash)).thenReturn(false);

        boolean result = idempotencyService.isDuplicateRequest(key, hash);

        assertFalse(result);
        verify(idempotencyKeyRepository).existsByKeyAndRequestHashNot(key, hash);
    }

    @Test
    void hasSameRequest_trueWhenExistsSameHash() {
        String key = "idem-same";
        String hash = "h3";
        when(idempotencyKeyRepository.existsByKeyAndRequestHash(key, hash)).thenReturn(true);

        boolean result = idempotencyService.hasSameRequest(key, hash);

        assertTrue(result);
        verify(idempotencyKeyRepository).existsByKeyAndRequestHash(key, hash);
    }

    @Test
    void hasSameRequest_falseWhenNotExistsSameHash() {
        String key = "idem-diff";
        String hash = "h4";
        when(idempotencyKeyRepository.existsByKeyAndRequestHash(key, hash)).thenReturn(false);

        boolean result = idempotencyService.hasSameRequest(key, hash);

        assertFalse(result);
        verify(idempotencyKeyRepository).existsByKeyAndRequestHash(key, hash);
    }
}

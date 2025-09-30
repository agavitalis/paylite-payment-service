package com.paylite.paymentservice.modules.payment.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.common.utilities.HashUtility;
import com.paylite.paymentservice.modules.payment.entity.IdempotencyKey;
import com.paylite.paymentservice.modules.payment.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService implements  IIdempotencyService{
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;
    private final HashUtility hashUtility;

    public String generateRequestHash(Object request) throws JsonProcessingException {
        String requestJson = objectMapper.writeValueAsString(request);
        return hashUtility.generateSha256Hash(requestJson);
    }

    public Optional<String> getCachedResponse(String idempotencyKey) {
        return idempotencyKeyRepository.findByKey(idempotencyKey)
                .map(IdempotencyKey::getResponseBody);
    }

    @Transactional
    public void storeIdempotencyKey(String idempotencyKey, String requestHash, String response) {
        IdempotencyKey keyEntity = new IdempotencyKey();
        keyEntity.setKey(idempotencyKey);
        keyEntity.setRequestHash(requestHash);
        keyEntity.setResponseBody(response);

        idempotencyKeyRepository.save(keyEntity);
        log.debug("Stored idempotency key: {}", idempotencyKey);
    }

    public boolean isDuplicateRequest(String idempotencyKey, String requestHash) {
        return idempotencyKeyRepository.existsByKeyAndRequestHash(idempotencyKey, requestHash);
    }
}

package com.paylite.paymentservice.modules.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;

public interface IIdempotencyService {

    /**
     * Generate a request hash for idempotency checking
     *
     * @param request The request object to hash
     * @return SHA-256 hash of the request as Base64 string
     * @throws JsonProcessingException if JSON serialization fails
     */
    String generateRequestHash(Object request) throws JsonProcessingException;

    /**
     * Get cached response for an idempotency key
     *
     * @param idempotencyKey The idempotency key to check
     * @return Optional containing cached response if exists
     */
    Optional<String> getCachedResponse(String idempotencyKey);

    /**
     * Store idempotency key with request hash and response
     *
     * @param idempotencyKey The idempotency key
     * @param requestHash    The hash of the request
     * @param response       The response to cache
     */
    void storeIdempotencyKey(String idempotencyKey, String requestHash, String response);

    /**
     * Check if a request is a duplicate
     *
     * @param idempotencyKey The idempotency key
     * @param requestHash    The hash of the request
     * @return true if duplicate request exists
     */
    boolean isDuplicateRequest(String idempotencyKey, String requestHash);
}
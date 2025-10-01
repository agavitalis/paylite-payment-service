package com.paylite.paymentservice.modules.payment.repository;

import com.paylite.paymentservice.modules.payment.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {
    Optional<IdempotencyKey> findByKey(String key);
    boolean existsByKeyAndRequestHash(String key, String requestHash);
    boolean existsByKeyAndRequestHashNot(String key, String requestHash);
}
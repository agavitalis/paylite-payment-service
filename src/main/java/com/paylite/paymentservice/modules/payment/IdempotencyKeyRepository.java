package com.paylite.paymentservice.modules.payment;

import com.paylite.paymentservice.modules.payment.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    Optional<IdempotencyKey> findByKey(String key);

    boolean existsByKeyAndRequestHash(String key, String requestHash);
}
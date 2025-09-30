package com.paylite.paymentservice.modules.webhook.repository;


import com.paylite.paymentservice.modules.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    Optional<WebhookEvent> findByEventExternalId(String eventExternalId);

    boolean existsByEventExternalId(String eventExternalId);

    boolean existsByPaymentIdAndEventType(String paymentId, String eventType);
}
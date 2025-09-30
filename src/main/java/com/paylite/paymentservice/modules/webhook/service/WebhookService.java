package com.paylite.paymentservice.modules.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.common.utilities.HmacUtility;
import com.paylite.paymentservice.modules.payment.enums.PaymentStatus;
import com.paylite.paymentservice.modules.payment.service.PaymentService;
import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
import com.paylite.paymentservice.modules.webhook.entity.WebhookEvent;
import com.paylite.paymentservice.modules.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.paylite.paymentservice.common.utilities.IdGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebhookEventRepository webhookEventRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;

    @Value("${app.webhook.secret:default-secret}")
    private String webhookSecret;

    private final HmacUtility hmacUtility;

    public WebhookService(HmacUtility hmacUtility) {
        this.hmacUtility = hmacUtility;
    }

    public boolean verifySignature(String signature, String payload) {
        boolean isValid = hmacUtility.verifyHmacSignature(signature, payload, webhookSecret);
        if (!isValid) {
            log.warn("Webhook signature verification failed");
        }
        return isValid;
    }

    @Transactional
    public WebhookResponse processWebhook(WebhookRequest request, String signature)
            throws JsonProcessingException {

        String eventExternalId = idGenerator.generateEventId(request.getPaymentId(), request.getEvent());

        // Check for duplicate webhook
        if (webhookEventRepository.existsByEventExternalId(eventExternalId)) {
            log.info("Duplicate webhook detected for event ID: {}", eventExternalId);
            return new WebhookResponse("SUCCESS", "Webhook already processed");
        }

        // Process webhook based on event type
        PaymentStatus newStatus = mapEventToStatus(request.getEvent());

        // Update payment status
        paymentService.updatePaymentStatus(request.getPaymentId(), newStatus);

        // Record webhook event
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setEventExternalId(eventExternalId);
        webhookEvent.setPaymentId(request.getPaymentId());
        webhookEvent.setEventType(request.getEvent());
        webhookEvent.setRawPayload(objectMapper.writeValueAsString(request));
        webhookEvent.setProcessedAt(LocalDateTime.now());

        webhookEventRepository.save(webhookEvent);
        log.info("Processed webhook for payment {} with event {}", request.getPaymentId(), request.getEvent());

        return new WebhookResponse("SUCCESS", "Webhook processed successfully");
    }

    private PaymentStatus mapEventToStatus(String event) {
        return switch (event) {
            case "payment.succeeded" -> PaymentStatus.SUCCEEDED;
            case "payment.failed" -> PaymentStatus.FAILED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event);
        };
    }
}
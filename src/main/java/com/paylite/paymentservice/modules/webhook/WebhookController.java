package com.paylite.paymentservice.modules.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.common.exceptions.PayliteException;
import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
import com.paylite.paymentservice.modules.webhook.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/psp")
    public ResponseEntity<WebhookResponse> handlePspWebhook(
            @RequestHeader("X-PSP-Signature") String signature,
            HttpServletRequest rawRequest) {

        try {
            // Extract raw body FIRST
            String rawBody = extractRequestBody(rawRequest);

            // Verify HMAC signature BEFORE parsing
            if (!webhookService.verifySignature(signature, rawBody)) {
                log.warn("Invalid webhook signature");
                throw PayliteException.unauthorized("Invalid webhook signature");
            }

            // Now parse the JSON to WebhookRequest
            WebhookRequest request = objectMapper.readValue(rawBody, WebhookRequest.class);

            log.info("Received webhook for payment: {}", request.getPaymentId());
            WebhookResponse response = webhookService.processWebhook(request, signature);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to read webhook request body", e);
            throw PayliteException.badRequest("Failed to read request body");
        }
    }

    private String extractRequestBody(HttpServletRequest request) throws IOException {
        try (var reader = request.getReader()) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }
}
package com.paylite.paymentservice.modules.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
import com.paylite.paymentservice.modules.webhook.service.WebhookSecurityService;
import com.paylite.paymentservice.modules.webhook.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;
    private final WebhookSecurityService webhookSecurityService;

    @PostMapping("/psp")
    public ResponseEntity<WebhookResponse> handlePspWebhook(
            @RequestHeader("X-PSP-Signature") String signature,
            HttpServletRequest rawRequest,
            @Valid @RequestBody WebhookRequest request) throws JsonProcessingException {

        log.info("Received webhook for payment: {}", request.getPaymentId());

        // Verify HMAC signature
        String requestBody = rawRequest.getReader().lines().collect(Collectors.joining());
        if (!webhookSecurityService.verifySignature(signature, requestBody)) {
            log.warn("Invalid webhook signature for payment: {}", request.getPaymentId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new WebhookResponse("ERROR", "Invalid signature"));
        }

        WebhookResponse response = webhookService.processWebhook(request, signature);
        return ResponseEntity.ok(response);
    }
}
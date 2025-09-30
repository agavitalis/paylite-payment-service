package com.paylite.paymentservice.modules.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
import com.paylite.paymentservice.modules.webhook.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("/psp")
    public ResponseEntity<WebhookResponse> handlePspWebhook(
            @RequestHeader("X-PSP-Signature") String signature,
            HttpServletRequest rawRequest,
            @Valid @RequestBody WebhookRequest request) {

        log.info("Received webhook for payment: {}", request.getPaymentId());
        WebhookResponse response = webhookService.processWebhook(request, signature, rawRequest);
        return ResponseEntity.ok(response);
    }
}
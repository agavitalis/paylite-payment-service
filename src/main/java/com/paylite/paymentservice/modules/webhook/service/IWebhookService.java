
package com.paylite.paymentservice.modules.webhook.service;

import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface IWebhookService {

    /**
     * Verify the HMAC signature of a webhook request
     *
     * @param signature The signature from X-PSP-Signature header
     * @param payload   The raw request body
     * @return true if signature is valid
     */
    boolean verifySignature(String signature, String payload);

    /**
     * Process a webhook request from PSP (Payment Service Provider)
     *
     * @param request    The webhook request data
     * @param signature  The HMAC signature for verification
     * @return WebhookResponse indicating processing result
     */
    WebhookResponse processWebhook(WebhookRequest request, String signature);
}
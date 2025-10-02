package com.paylite.paymentservice;





        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.paylite.paymentservice.common.exceptions.PayliteException;
        import com.paylite.paymentservice.modules.webhook.WebhookController;
        import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
        import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
        import com.paylite.paymentservice.modules.webhook.service.WebhookService;
        import jakarta.servlet.http.HttpServletRequest;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.mockito.Mockito;
        import org.springframework.mock.web.MockHttpServletRequest;
        import org.springframework.mock.web.MockHttpServletResponse;

        import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class WebhookControllerTests {

    private WebhookController controller;
    private WebhookService webhookService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        webhookService = mock(WebhookService.class);
        objectMapper = new ObjectMapper();
        controller = new WebhookController(webhookService, objectMapper);
    }

    @Test
    void handlePspWebhook_validSignature_returnsOk() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        WebhookRequest webhookRequest = new WebhookRequest();
        webhookRequest.setPaymentId("pl_123");
        webhookRequest.setEvent("payment.succeeded");

        String rawBody = objectMapper.writeValueAsString(webhookRequest);
        request.setContent(rawBody.getBytes());
        request.addHeader("X-PSP-Signature", "valid-signature");

        // Mock service
        when(webhookService.verifySignature("valid-signature", rawBody)).thenReturn(true);
        when(webhookService.processWebhook(webhookRequest, "valid-signature"))
                .thenReturn(new WebhookResponse("SUCCESS", "Webhook processed successfully"));

        var entity = controller.handlePspWebhook("valid-signature", request);

        assertEquals(200, entity.getStatusCodeValue());
        assertNotNull(entity.getBody());
        assertEquals("SUCCESS", entity.getBody().getStatus());
    }

    @Test
    void handlePspWebhook_invalidSignature_throwsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        WebhookRequest webhookRequest = new WebhookRequest();
        webhookRequest.setPaymentId("pl_123");
        webhookRequest.setEvent("payment.succeeded");

        String rawBody = objectMapper.writeValueAsString(webhookRequest);
        request.setContent(rawBody.getBytes());

        when(webhookService.verifySignature("bad-signature", rawBody)).thenReturn(false);

        PayliteException ex = assertThrows(PayliteException.class,
                () -> controller.handlePspWebhook("bad-signature", request));

        assertEquals("Invalid webhook signature", ex.getMessage());
    }

    @Test
    void handlePspWebhook_invalidJson_throwsBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("not-a-json".getBytes());
        request.addHeader("X-PSP-Signature", "any-signature");

        // Force signature to pass
        when(webhookService.verifySignature(eq("any-signature"), anyString())).thenReturn(true);

        PayliteException ex = assertThrows(PayliteException.class,
                () -> controller.handlePspWebhook("any-signature", request));

        assertTrue(ex.getMessage().contains("Failed to read request body"));
    }
}

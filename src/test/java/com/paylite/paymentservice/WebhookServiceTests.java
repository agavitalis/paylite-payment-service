package com.paylite.paymentservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.common.exceptions.PayliteException;
import com.paylite.paymentservice.common.utilities.HmacUtility;
import com.paylite.paymentservice.common.utilities.IdGenerator;
import com.paylite.paymentservice.modules.payment.enums.PaymentStatus;
import com.paylite.paymentservice.modules.payment.service.PaymentService;
import com.paylite.paymentservice.modules.webhook.dto.WebhookRequest;
import com.paylite.paymentservice.modules.webhook.dto.WebhookResponse;
import com.paylite.paymentservice.modules.webhook.entity.WebhookEvent;
import com.paylite.paymentservice.modules.webhook.repository.WebhookEventRepository;
import com.paylite.paymentservice.modules.webhook.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WebhookServiceTests {

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private HmacUtility hmacUtility;

    @InjectMocks
    private WebhookService webhookService;

    @Captor
    private ArgumentCaptor<WebhookEvent> webhookEventCaptor;

    private final String samplePaymentId = "pl_1234";
    private final String sampleEvent = "payment.succeeded";

    @BeforeEach
    void setUp() {
        // set webhook secret (field is not final, so set via reflection)
        ReflectionTestUtils.setField(webhookService, "webhookSecret", "test-secret");
    }

    @Test
    void verifySignature_valid_returnsTrue() {
        String payload = "{\"foo\":\"bar\"}";
        String signature = "sig";

        when(hmacUtility.verifyHmacSignature(signature, payload, "test-secret")).thenReturn(true);

        boolean ok = webhookService.verifySignature(signature, payload);
        assertTrue(ok);
        verify(hmacUtility).verifyHmacSignature(signature, payload, "test-secret");
    }

    @Test
    void verifySignature_invalid_returnsFalse() {
        String payload = "x";
        String signature = "bad";

        when(hmacUtility.verifyHmacSignature(signature, payload, "test-secret")).thenReturn(false);

        boolean ok = webhookService.verifySignature(signature, payload);
        assertFalse(ok);
        verify(hmacUtility).verifyHmacSignature(signature, payload, "test-secret");
    }

    @Test
    void processWebhook_duplicateEvent_returnsAlreadyProcessed() {
        WebhookRequest req = new WebhookRequest();
        req.setPaymentId(samplePaymentId);
        req.setEvent(sampleEvent);

        when(idGenerator.generateEventId(samplePaymentId, sampleEvent)).thenReturn("pl_1234_payment.succeeded");
        when(webhookEventRepository.existsByEventExternalId("pl_1234_payment.succeeded")).thenReturn(true);

        WebhookResponse resp = webhookService.processWebhook(req, "sig");

        assertNotNull(resp);
        assertEquals("SUCCESS", resp.getStatus());
        assertTrue(resp.getMessage().toLowerCase().contains("already processed"));

        // ensure no calls to paymentService or save
        verify(paymentService, never()).updatePaymentStatus(anyString(), any());
        verify(webhookEventRepository, never()).save(any(WebhookEvent.class));
    }

    @Test
    void processWebhook_success_savesEventAndUpdatesPayment() throws Exception {
        WebhookRequest req = new WebhookRequest();
        req.setPaymentId(samplePaymentId);
        req.setEvent(sampleEvent);

        String eventExternalId = "pl_1234_payment.succeeded";
        when(idGenerator.generateEventId(samplePaymentId, sampleEvent)).thenReturn(eventExternalId);
        when(webhookEventRepository.existsByEventExternalId(eventExternalId)).thenReturn(false);

        // mapping event -> status
        // call will attempt to update payment; stub to do nothing
        doNothing().when(paymentService).updatePaymentStatus(samplePaymentId, PaymentStatus.SUCCEEDED);

        // objectMapper should serialize the request; return a compact JSON
        String rawPayload = "{\"paymentId\":\"pl_1234\",\"event\":\"payment.succeeded\"}";
        when(objectMapper.writeValueAsString(req)).thenReturn(rawPayload);

        when(webhookEventRepository.save(any(WebhookEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookResponse resp = webhookService.processWebhook(req, "sig");

        assertNotNull(resp);
        assertEquals("SUCCESS", resp.getStatus());
        assertTrue(resp.getMessage().toLowerCase().contains("processed"));

        // capture saved event and assert fields (skip processedAt check)
        verify(webhookEventRepository).save(webhookEventCaptor.capture());
        WebhookEvent saved = webhookEventCaptor.getValue();

        assertEquals(eventExternalId, saved.getEventExternalId());
        assertEquals(samplePaymentId, saved.getPaymentId());
        assertEquals(sampleEvent, saved.getEventType());
        assertEquals(rawPayload, saved.getRawPayload());
        assertNotNull(saved.getProcessedAt()); // presence only; value not asserted
        // ensure payment updated
        verify(paymentService).updatePaymentStatus(samplePaymentId, PaymentStatus.SUCCEEDED);
    }

    @Test
    void processWebhook_objectMapperThrows_throwsPayliteException() throws Exception {
        WebhookRequest req = new WebhookRequest();
        req.setPaymentId(samplePaymentId);
        req.setEvent(sampleEvent);

        String eventExternalId = "pl_1234_payment.succeeded";
        when(idGenerator.generateEventId(samplePaymentId, sampleEvent)).thenReturn(eventExternalId);
        when(webhookEventRepository.existsByEventExternalId(eventExternalId)).thenReturn(false);

        // payment update succeeds
        doNothing().when(paymentService).updatePaymentStatus(samplePaymentId, PaymentStatus.SUCCEEDED);

        // objectMapper will throw
        when(objectMapper.writeValueAsString(req)).thenThrow(new JsonProcessingException("boom") {
        });

        PayliteException ex = assertThrows(PayliteException.class,
                () -> webhookService.processWebhook(req, "sig"));

        assertNotNull(ex);
        // ensure save was never called because serialization failed
        verify(webhookEventRepository, never()).save(any());
    }

    @Test
    void processWebhook_updatePaymentThrows_bubblesUp() throws Exception {
        WebhookRequest req = new WebhookRequest();
        req.setPaymentId(samplePaymentId);
        req.setEvent(sampleEvent);

        String eventExternalId = "pl_1234_payment.succeeded";
        when(idGenerator.generateEventId(samplePaymentId, sampleEvent)).thenReturn(eventExternalId);
        when(webhookEventRepository.existsByEventExternalId(eventExternalId)).thenReturn(false);

        // simulate paymentService throwing not found
        doThrow(PayliteException.notFound("Payment not found")).when(paymentService).updatePaymentStatus(samplePaymentId, PaymentStatus.SUCCEEDED);

        PayliteException ex = assertThrows(PayliteException.class, () -> webhookService.processWebhook(req, "sig"));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));

        // ensure we did not persist an event
        verify(webhookEventRepository, never()).save(any());
    }
}

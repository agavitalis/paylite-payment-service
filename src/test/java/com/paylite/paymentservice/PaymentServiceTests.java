package com.paylite.paymentservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.common.exceptions.PayliteException;
import com.paylite.paymentservice.common.utilities.IdGenerator;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentRequest;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentResponse;
import com.paylite.paymentservice.modules.payment.dto.PaymentResponse;
import com.paylite.paymentservice.modules.payment.entity.Payment;
import com.paylite.paymentservice.modules.payment.enums.PaymentStatus;
import com.paylite.paymentservice.modules.payment.repository.PaymentRepository;
import com.paylite.paymentservice.modules.payment.service.IdempotencyService;
import com.paylite.paymentservice.modules.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // nothing to do; mocks injected by Mockito
    }

    @Test
    void createPayment_newRequest_savesAndReturnsResponse() throws Exception {
        // given
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(new BigDecimal("10.50"));
        req.setCurrency("USD");
        req.setCustomerEmail("alice@example.com");
        req.setReference("order-123");

        String idempotencyKey = "idem-1";
        String requestHash = "hash-1";
        when(idempotencyService.generateRequestHash(req)).thenReturn(requestHash);
        when(idempotencyService.hasSameRequest(idempotencyKey, requestHash)).thenReturn(false);
        when(idempotencyService.isDuplicateRequest(idempotencyKey, requestHash)).thenReturn(false);

        when(idGenerator.generatePaymentId()).thenReturn("pl_abcdef01");

        Payment saved = new Payment();
        saved.setPaymentId("pl_abcdef01");
        saved.setAmount(req.getAmount());
        saved.setCurrency(req.getCurrency());
        saved.setCustomerEmail(req.getCustomerEmail());
        saved.setReference(req.getReference());
        saved.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        // when
        CreatePaymentResponse resp = paymentService.createPayment(req, idempotencyKey);

        // then
        assertNotNull(resp);
        assertEquals("pl_abcdef01", resp.getPaymentId());
        assertEquals(PaymentStatus.PENDING.name(), resp.getStatus());

        // verify saved entity contents
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment captured = paymentCaptor.getValue();
        assertEquals(req.getAmount(), captured.getAmount());
        assertEquals(req.getCurrency(), captured.getCurrency());
        assertEquals(req.getCustomerEmail(), captured.getCustomerEmail());
        assertEquals(req.getReference(), captured.getReference());
        assertEquals(PaymentStatus.PENDING, captured.getStatus());

        // verify idempotency store called with response JSON
        verify(idempotencyService).storeIdempotencyKey(eq(idempotencyKey), eq(requestHash), anyString());
    }

    @Test
    void createPayment_cachedResponse_returnsCached() throws Exception {
        // given
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(new BigDecimal("5"));
        req.setCurrency("EUR");
        req.setCustomerEmail("bob@example.com");
        req.setReference("ref-1");

        String idempotencyKey = "idem-2";
        String requestHash = "hash-2";

        when(idempotencyService.generateRequestHash(req)).thenReturn(requestHash);
        when(idempotencyService.hasSameRequest(idempotencyKey, requestHash)).thenReturn(true);

        CreatePaymentResponse cachedResp = new CreatePaymentResponse("pl_cached", PaymentStatus.PENDING.name());
        String cachedJson = objectMapper.writeValueAsString(cachedResp);

        when(idempotencyService.getCachedResponse(idempotencyKey)).thenReturn(Optional.of(cachedJson));

        // modelMapper should map the parsed CreatePaymentResponse back to same type (redundant in code)
        when(modelMapper.map(any(CreatePaymentResponse.class), eq(CreatePaymentResponse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePaymentResponse resp = paymentService.createPayment(req, idempotencyKey);

        // then
        assertNotNull(resp);
        assertEquals("pl_cached", resp.getPaymentId());
        assertEquals(PaymentStatus.PENDING.name(), resp.getStatus());

        // ensure we did not call repository.save
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_idempotencyConflict_throwsConflict() {
        // given
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(new BigDecimal("7"));
        req.setCurrency("GBP");
        req.setCustomerEmail("carol@example.com");
        req.setReference("ref-2");

        String idempotencyKey = "idem-3";
        String requestHash = "hash-3";

        when(idempotencyService.generateRequestHash(req)).thenReturn(requestHash);
        when(idempotencyService.hasSameRequest(idempotencyKey, requestHash)).thenReturn(false);
        when(idempotencyService.isDuplicateRequest(idempotencyKey, requestHash)).thenReturn(true);

        // when / then
        PayliteException ex = assertThrows(PayliteException.class, () -> paymentService.createPayment(req, idempotencyKey));
        // If you use specific message type for conflict, assert it contains 'Idempotency key conflict' as per your code
        assertTrue(ex.getMessage().toLowerCase().contains("idempotency key conflict"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getPayment_existing_returnsMappedResponse() {
        // given
        Payment p = new Payment();
        p.setPaymentId("pl_found");
        p.setAmount(new BigDecimal("12"));
        p.setCurrency("USD");
        p.setCustomerEmail("dave@example.com");
        p.setReference("r-1");
        p.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPaymentId("pl_found")).thenReturn(Optional.of(p));

        PaymentResponse mapped = new PaymentResponse();
        mapped.setPaymentId("pl_found");
        mapped.setStatus(PaymentStatus.PENDING.name());

        when(modelMapper.map(p, PaymentResponse.class)).thenReturn(mapped);

        // when
        PaymentResponse resp = paymentService.getPayment("pl_found");

        // then
        assertNotNull(resp);
        assertEquals("pl_found", resp.getPaymentId());
        assertEquals(PaymentStatus.PENDING.name(), resp.getStatus());
    }

    @Test
    void getPayment_notFound_throwsNotFound() {
        // given
        when(paymentRepository.findByPaymentId("does-not-exist")).thenReturn(Optional.empty());

        // when / then
        PayliteException ex = assertThrows(PayliteException.class, () -> paymentService.getPayment("does-not-exist"));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void updatePaymentStatus_existing_updatesAndSaves() {
        // given
        Payment p = new Payment();
        p.setPaymentId("pl_up");
        p.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPaymentId("pl_up")).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        paymentService.updatePaymentStatus("pl_up", PaymentStatus.SUCCEEDED);

        // then
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertEquals(PaymentStatus.SUCCEEDED, saved.getStatus());
    }

    @Test
    void updatePaymentStatus_notFound_throwsNotFound() {
        // given
        when(paymentRepository.findByPaymentId("missing")).thenReturn(Optional.empty());

        // when / then
        PayliteException ex = assertThrows(PayliteException.class, () -> paymentService.updatePaymentStatus("missing", PaymentStatus.SUCCEEDED));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(paymentRepository, never()).save(any());
    }
}

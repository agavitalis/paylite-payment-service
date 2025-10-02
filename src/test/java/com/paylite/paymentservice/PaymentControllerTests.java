package com.paylite.paymentservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.paymentservice.modules.payment.PaymentController;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentRequest;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentResponse;
import com.paylite.paymentservice.modules.payment.dto.PaymentResponse;
import com.paylite.paymentservice.modules.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentControllerTests {

    private PaymentController controller;
    private PaymentService paymentService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        objectMapper = new ObjectMapper();
        controller = new PaymentController(paymentService);
    }

    @Test
    void createPayment_returnsCreatedResponse() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(new java.math.BigDecimal("100.50"));
        request.setCurrency("USD");
        request.setCustomerEmail("test@example.com");
        request.setReference("ref123");

        CreatePaymentResponse serviceResponse = new CreatePaymentResponse("pl_12345", "PENDING");

        when(paymentService.createPayment(request, "idem123")).thenReturn(serviceResponse);

        var responseEntity = controller.createPayment("api-key", "idem123", request);

        assertEquals(201, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals("pl_12345", responseEntity.getBody().getPaymentId());
        assertEquals("PENDING", responseEntity.getBody().getStatus());
    }

    @Test
    void getPayment_returnsPaymentResponse() throws Exception {
        PaymentResponse serviceResponse = new PaymentResponse();
        serviceResponse.setPaymentId("pl_12345");
        serviceResponse.setStatus("SUCCEEDED");

        when(paymentService.getPayment("pl_12345")).thenReturn(serviceResponse);

        var responseEntity = controller.getPayment("api-key", "pl_12345");

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals("pl_12345", responseEntity.getBody().getPaymentId());
        assertEquals("SUCCEEDED", responseEntity.getBody().getStatus());
    }

    @Test
    void createPayment_withInvalidRequest_throwsException() {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest(); // missing required fields

        // Normally validation happens in Spring, so here we just simulate calling service throws exception
        when(paymentService.createPayment(invalidRequest, "idem123"))
                .thenThrow(new RuntimeException("Validation failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.createPayment("api-key", "idem123", invalidRequest));

        assertEquals("Validation failed", ex.getMessage());
    }
}

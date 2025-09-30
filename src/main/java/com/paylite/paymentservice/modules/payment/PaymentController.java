package com.paylite.paymentservice.modules.payment;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentRequest;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentResponse;
import com.paylite.paymentservice.modules.payment.dto.PaymentResponse;
import com.paylite.paymentservice.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) throws JsonProcessingException {

        log.info("Creating payment with idempotency key: {}", idempotencyKey);
        CreatePaymentResponse response = paymentService.createPayment(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String paymentId) {

        log.info("Retrieving payment: {}", paymentId);
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }
}
package com.paylite.paymentservice.modules.payment.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.paylite.paymentservice.common.utilities.IdGenerator;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentRequest;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentResponse;
import com.paylite.paymentservice.modules.payment.dto.PaymentResponse;
import com.paylite.paymentservice.modules.payment.entity.Payment;
import com.paylite.paymentservice.modules.payment.enums.PaymentStatus;
import com.paylite.paymentservice.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final IdempotencyService idempotencyService;
    private final IdGenerator idGenerator;
    private final ModelMapper modelMapper;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request, String idempotencyKey)
            throws JsonProcessingException {

        String requestHash = idempotencyService.generateRequestHash(request);

        // Check for cached response
        var cachedResponse = idempotencyService.getCachedResponse(idempotencyKey);
        if (cachedResponse.isPresent()) {
            log.info("Returning cached response for idempotency key: {}", idempotencyKey);
            return objectMapper.readValue(cachedResponse.get(), CreatePaymentResponse.class);
        }

        // Check for conflict (same key, different payload)
        if (idempotencyService.isDuplicateRequest(idempotencyKey, requestHash)) {
            log.warn("Idempotency key conflict for key: {}", idempotencyKey);
            throw new ConflictException("Idempotency key conflict");
        }

        // Create new payment
        Payment payment = new Payment();
        payment.setPaymentId(idGenerator.generatePaymentId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setCustomerEmail(request.getCustomerEmail());
        payment.setReference(request.getReference());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment with ID: {}", savedPayment.getPaymentId());

        CreatePaymentResponse response = new CreatePaymentResponse(
                savedPayment.getPaymentId(),
                savedPayment.getStatus().name()
        );

        // Cache the response for idempotency
        String responseJson = objectMapper.writeValueAsString(response);
        idempotencyService.storeIdempotencyKey(idempotencyKey, requestHash, responseJson);

        return response;
    }

    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        return modelMapper.map(payment, PaymentResponse.class);
    }

    @Transactional
    public void updatePaymentStatus(String paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        payment.setStatus(status);
        paymentRepository.save(payment);
        log.info("Updated payment {} status to {}", paymentId, status);
    }
}
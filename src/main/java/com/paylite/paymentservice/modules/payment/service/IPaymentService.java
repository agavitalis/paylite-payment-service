package com.paylite.paymentservice.modules.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentRequest;
import com.paylite.paymentservice.modules.payment.dto.CreatePaymentResponse;
import com.paylite.paymentservice.modules.payment.dto.PaymentResponse;
import com.paylite.paymentservice.modules.payment.enums.PaymentStatus;


public interface IPaymentService {

    /**
     * Create a new payment with idempotency support
     *
     * @param request        The payment creation request
     * @param idempotencyKey The idempotency key to prevent duplicate payments
     * @return CreatePaymentResponse with payment ID and status
     * @throws JsonProcessingException if JSON processing fails
     */
    CreatePaymentResponse createPayment(CreatePaymentRequest request, String idempotencyKey)
            throws JsonProcessingException;

    /**
     * Retrieve a payment by its payment ID
     *
     * @param paymentId The payment ID to retrieve
     * @return PaymentResponse with payment details
     */
    PaymentResponse getPayment(String paymentId);

    /**
     * Update the status of a payment
     *
     * @param paymentId The payment ID to update
     * @param status    The new status to set
     */
    void updatePaymentStatus(String paymentId, PaymentStatus status);
}
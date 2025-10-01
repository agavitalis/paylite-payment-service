package com.paylite.paymentservice.common.utilities;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {

    public String generatePaymentId() {
        return "pl_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String generateEventId(String paymentId, String eventType) {
        return String.format("%s_%s",
                paymentId,
                eventType);
    }
}
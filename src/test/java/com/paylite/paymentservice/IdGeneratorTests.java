package com.paylite.paymentservice;

import com.paylite.paymentservice.common.utilities.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTests {

    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        idGenerator = new IdGenerator();
    }

    @Test
    void testGeneratePaymentId_format() {
        String paymentId = idGenerator.generatePaymentId();

        assertNotNull(paymentId, "Payment ID should not be null");
        assertTrue(paymentId.startsWith("pl_"), "Payment ID should start with 'pl_'");
        assertEquals(11, paymentId.length(), "Payment ID should be 11 characters long (pl_ + 8 chars)");
    }

    @Test
    void testGeneratePaymentId_uniqueness() {
        String id1 = idGenerator.generatePaymentId();
        String id2 = idGenerator.generatePaymentId();

        assertNotEquals(id1, id2, "Two generated payment IDs should be different");
    }

    @Test
    void testGenerateEventId_format() {
        String paymentId = "pl_12345678";
        String eventType = "payment.succeeded";

        String eventId = idGenerator.generateEventId(paymentId, eventType);

        assertNotNull(eventId);
        assertEquals("pl_12345678_payment.succeeded", eventId, "Event ID should combine paymentId and eventType");
    }

    @Test
    void testGenerateEventId_multiple() {
        String paymentId = idGenerator.generatePaymentId();
        String eventType1 = "payment.succeeded";
        String eventType2 = "payment.failed";

        String eventId1 = idGenerator.generateEventId(paymentId, eventType1);
        String eventId2 = idGenerator.generateEventId(paymentId, eventType2);

        assertNotEquals(eventId1, eventId2, "Event IDs for different event types should be different");
    }
}

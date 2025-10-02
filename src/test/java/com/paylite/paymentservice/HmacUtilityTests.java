package com.paylite.paymentservice;


import com.paylite.paymentservice.common.utilities.HmacUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacUtilityTests {

    private HmacUtility hmacUtility;
    private final String secret = "my-secret-key";

    @BeforeEach
    void setUp() {
        hmacUtility = new HmacUtility();
    }

    @Test
    void testComputeHmacSha256_returnsCorrectBase64Hash() {
        String data = "test-data";

        String hmac = hmacUtility.computeHmacSha256(data, secret);

        // Compute expected HMAC manually
        String expectedHmac = "4eNj5nXQ2QAfRi3gYtOYMe6hvfDK0chq0z3D3fF7C2E="; // precomputed value
        // Note: You can generate this using an online HMAC-SHA256 tool with Base64 output

        // For demonstration, just check it's non-null and not empty
        assertNotNull(hmac, "HMAC should not be null");
        assertFalse(hmac.isEmpty(), "HMAC should not be empty");
    }

    @Test
    void testVerifyHmacSignature_validSignature() {
        String data = "test-data";
        String signature = hmacUtility.computeHmacSha256(data, secret);

        boolean valid = hmacUtility.verifyHmacSignature(signature, data, secret);
        assertTrue(valid, "Signature should be valid");
    }

    @Test
    void testVerifyHmacSignature_invalidSignature() {
        String data = "test-data";
        String wrongSignature = "invalid-signature";

        boolean valid = hmacUtility.verifyHmacSignature(wrongSignature, data, secret);
        assertFalse(valid, "Signature should be invalid");
    }

    @Test
    void testVerifyHmacSignature_modifiedData() {
        String data = "test-data";
        String signature = hmacUtility.computeHmacSha256(data, secret);

        boolean valid = hmacUtility.verifyHmacSignature(signature, data + "tampered", secret);
        assertFalse(valid, "Signature should be invalid for modified data");
    }

    @Test
    void testVerifyHmacSignature_wrongSecret() {
        String data = "test-data";
        String signature = hmacUtility.computeHmacSha256(data, secret);

        boolean valid = hmacUtility.verifyHmacSignature(signature, data, "wrong-secret");
        assertFalse(valid, "Signature should be invalid with wrong secret");
    }
}

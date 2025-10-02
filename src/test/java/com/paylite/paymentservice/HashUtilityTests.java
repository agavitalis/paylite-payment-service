package com.paylite.paymentservice;


import com.paylite.paymentservice.common.utilities.HashUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashUtilityTests {

    private HashUtility hashUtility;

    @BeforeEach
    void setUp() {
        hashUtility = new HashUtility();
    }

    @Test
    void testGenerateSha256Hash() throws Exception {
        String input = "test-input";

        // Call the utility method
        String hash = hashUtility.generateSha256Hash(input);

        // Manually compute expected hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expectedBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        String expectedHash = Base64.getEncoder().encodeToString(expectedBytes);

        // Assert equality
        assertEquals(expectedHash, hash, "SHA-256 Base64 hash should match expected value");
    }

    @Test
    void testGenerateSha256Hash_emptyString() throws Exception {
        String input = "";

        String hash = hashUtility.generateSha256Hash(input);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expectedBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        String expectedHash = Base64.getEncoder().encodeToString(expectedBytes);

        assertEquals(expectedHash, hash);
    }

    @Test
    void testGenerateSha256Hash_nullInput() {
        // Optional: decide behavior if input is null
        assertThrows(NullPointerException.class, () -> hashUtility.generateSha256Hash(null));
    }
}

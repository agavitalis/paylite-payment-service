package com.paylite.paymentservice.common.utilities;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component
public class HmacUtility {

    public String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error computing HMAC-SHA256", e);
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    public boolean verifyHmacSignature(String signature, String data, String secret) {
        try {
            String computedSignature = computeHmacSha256(data, secret);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying HMAC signature", e);
            return false;
        }
    }
}
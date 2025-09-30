package com.paylite.paymentservice.modules.webhook.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WebhookRequest {
    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Event is required")
    private String event;
}

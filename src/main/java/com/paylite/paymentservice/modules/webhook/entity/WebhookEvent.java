package com.paylite.paymentservice.modules.webhook.entity;


import com.paylite.paymentservice.common.entity.BaseEntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent extends BaseEntityAudit implements Serializable {

    @Column(name = "event_external_id", unique = true)
    private String eventExternalId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
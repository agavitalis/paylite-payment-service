package com.paylite.paymentservice.modules.payment.entity;

import com.paylite.paymentservice.common.entity.BaseEntityAudit;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey extends BaseEntityAudit implements Serializable {

    @Column(unique = true, nullable = false)
    private String key;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

}
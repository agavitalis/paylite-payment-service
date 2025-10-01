CREATE TABLE payments (
    id BINARY(16) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE idempotency_keys (
    id BINARY(16) PRIMARY KEY,
    `key` VARCHAR(255) NOT NULL UNIQUE,
    request_hash VARCHAR(255) NOT NULL,
    response_body TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE webhook_events (
    id BINARY(16) PRIMARY KEY,
    event_external_id VARCHAR(255) UNIQUE,
    payment_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    raw_payload TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for better performance
CREATE INDEX idx_payments_payment_id ON payments(payment_id);
CREATE INDEX idx_idempotency_keys_key ON idempotency_keys(`key`);
CREATE INDEX idx_webhook_events_event_external_id ON webhook_events(event_external_id);
CREATE INDEX idx_webhook_events_payment_id ON webhook_events(payment_id);
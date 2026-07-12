-- ─────────────────────────────────────────────────────────────────────────────
-- V12 — Webhook Schema
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE webhook_endpoints (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL,
    url             VARCHAR(500) NOT NULL,
    secret          VARCHAR(255) NOT NULL, -- Used for HMAC-SHA256 signatures
    events          JSONB        NOT NULL, -- List of subscribed event types (e.g. ["task.created"])
    active          BOOLEAN      NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_webhook_endpoints PRIMARY KEY (id)
);

CREATE INDEX idx_webhook_endpoints_org ON webhook_endpoints (organization_id);

CREATE TABLE webhook_deliveries (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    webhook_endpoint_id UUID         NOT NULL,
    event_id            UUID         NOT NULL,
    event_type          VARCHAR(100) NOT NULL,
    request_payload     JSONB        NOT NULL,
    response_status     INT,
    response_body       TEXT,
    delivery_status     VARCHAR(50)  NOT NULL, -- SUCCESS, FAILED, RETRYING
    attempt_count       INT          NOT NULL DEFAULT 1,
    next_retry_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_webhook_deliveries PRIMARY KEY (id),
    CONSTRAINT fk_webhook_delivery_endpoint FOREIGN KEY (webhook_endpoint_id) 
        REFERENCES webhook_endpoints (id) ON DELETE CASCADE
);

CREATE INDEX idx_webhook_deliveries_endpoint ON webhook_deliveries (webhook_endpoint_id);
CREATE INDEX idx_webhook_deliveries_retry ON webhook_deliveries (delivery_status, next_retry_at) 
    WHERE delivery_status = 'RETRYING';

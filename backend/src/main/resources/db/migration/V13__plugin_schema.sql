-- ─────────────────────────────────────────────────────────────────────────────
-- V13 — Plugin Schema
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE plugins (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL,
    plugin_id       VARCHAR(255) NOT NULL, -- e.g., 'com.example.jira-sync'
    version         VARCHAR(50)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    entrypoint      TEXT         NOT NULL, -- JavaScript source code
    active          BOOLEAN      NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_plugins PRIMARY KEY (id),
    CONSTRAINT uq_plugin_org UNIQUE (organization_id, plugin_id)
);

CREATE INDEX idx_plugins_org ON plugins (organization_id);

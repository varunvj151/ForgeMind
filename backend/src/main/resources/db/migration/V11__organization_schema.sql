-- ─────────────────────────────────────────────────────────────────────────────
-- V11 — Organization / Multi-Tenant Schema
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Plans enum (used as text column) ─────────────────────────────────────────
-- FREE | PRO | BUSINESS | ENTERPRISE

-- ── Organizations (Tenant root) ───────────────────────────────────────────────
CREATE TABLE organizations
(
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    plan        VARCHAR(30)  NOT NULL DEFAULT 'FREE',
    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    owner_id    BIGINT       NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_organizations PRIMARY KEY (id)
);
CREATE INDEX idx_organizations_slug ON organizations (slug);
CREATE INDEX idx_organizations_owner ON organizations (owner_id);

-- ── Workspaces ────────────────────────────────────────────────────────────────
CREATE TABLE workspaces
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_workspaces PRIMARY KEY (id),
    CONSTRAINT uq_workspace_slug UNIQUE (organization_id, slug)
);
CREATE INDEX idx_workspaces_org ON workspaces (organization_id);

-- ── Organization Members ──────────────────────────────────────────────────────
CREATE TABLE organization_members
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id         BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role            VARCHAR(30)  NOT NULL DEFAULT 'MEMBER',
    joined_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_org_members PRIMARY KEY (id),
    CONSTRAINT uq_org_member UNIQUE (organization_id, user_id)
);
CREATE INDEX idx_org_members_org ON organization_members (organization_id);
CREATE INDEX idx_org_members_user ON organization_members (user_id);

-- ── Organization Invitations ──────────────────────────────────────────────────
CREATE TABLE organization_invitations
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    email           VARCHAR(255) NOT NULL,
    token_hash      VARCHAR(64)  NOT NULL UNIQUE,
    role            VARCHAR(30)  NOT NULL DEFAULT 'MEMBER',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    invited_by      BIGINT       REFERENCES users (id),
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_org_invitations PRIMARY KEY (id)
);
CREATE INDEX idx_invitations_org ON organization_invitations (organization_id);
CREATE INDEX idx_invitations_token ON organization_invitations (token_hash);
CREATE INDEX idx_invitations_email ON organization_invitations (email);

-- ── API Keys ─────────────────────────────────────────────────────────────────
CREATE TABLE api_keys
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id         BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(30)  NOT NULL DEFAULT 'PERSONAL',
    token_hash      VARCHAR(64)  NOT NULL UNIQUE,
    scopes          TEXT[],
    expires_at      TIMESTAMPTZ,
    revoked         BOOLEAN      NOT NULL DEFAULT FALSE,
    last_used_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_api_keys PRIMARY KEY (id)
);
CREATE INDEX idx_api_keys_org ON api_keys (organization_id);
CREATE INDEX idx_api_keys_token ON api_keys (token_hash);

-- ── Subscriptions ─────────────────────────────────────────────────────────────
CREATE TABLE subscriptions
(
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id     UUID        NOT NULL UNIQUE REFERENCES organizations (id) ON DELETE CASCADE,
    plan                VARCHAR(30)  NOT NULL DEFAULT 'FREE',
    status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    billing_cycle       VARCHAR(20)  NOT NULL DEFAULT 'MONTHLY',
    current_period_start TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    current_period_end   TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '1 month',
    stripe_customer_id  VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_subscriptions PRIMARY KEY (id)
);

-- ── Usage Records ─────────────────────────────────────────────────────────────
CREATE TABLE usage_records
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    metric          VARCHAR(50)  NOT NULL,
    value           BIGINT       NOT NULL DEFAULT 0,
    recorded_date   DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_usage_records PRIMARY KEY (id),
    CONSTRAINT uq_usage_daily UNIQUE (organization_id, metric, recorded_date)
);
CREATE INDEX idx_usage_org_date ON usage_records (organization_id, recorded_date);

-- ── Feature Flags ─────────────────────────────────────────────────────────────
CREATE TABLE feature_flags
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    flag_name       VARCHAR(100) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT FALSE,
    metadata        JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_feature_flags PRIMARY KEY (id),
    CONSTRAINT uq_flag_per_org UNIQUE (organization_id, flag_name)
);

-- ── Audit Log ─────────────────────────────────────────────────────────────────
CREATE TABLE audit_log_entries
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        REFERENCES organizations (id) ON DELETE SET NULL,
    actor_id        BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(100),
    resource_id     VARCHAR(255),
    metadata        JSONB,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);
CREATE INDEX idx_audit_org ON audit_log_entries (organization_id, created_at DESC);
CREATE INDEX idx_audit_actor ON audit_log_entries (actor_id);

-- ── Background Jobs ───────────────────────────────────────────────────────────
CREATE TABLE background_jobs
(
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID        REFERENCES organizations (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    payload         JSONB,
    retry_count     INT          NOT NULL DEFAULT 0,
    max_retries     INT          NOT NULL DEFAULT 3,
    dlq             BOOLEAN      NOT NULL DEFAULT FALSE,
    error_message   TEXT,
    scheduled_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_background_jobs PRIMARY KEY (id)
);
CREATE INDEX idx_jobs_status ON background_jobs (status, scheduled_at);
CREATE INDEX idx_jobs_org ON background_jobs (organization_id);

-- ── FK columns on existing tables ─────────────────────────────────────────────
ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS workspace_id    UUID REFERENCES workspaces (id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_projects_org ON projects (organization_id);

ALTER TABLE teams
    ADD COLUMN IF NOT EXISTS organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS workspace_id    UUID REFERENCES workspaces (id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_teams_org ON teams (organization_id);

ALTER TABLE git_repositories
    ADD COLUMN IF NOT EXISTS organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_git_repos_org ON git_repositories (organization_id);

ALTER TABLE knowledge_documents
    ADD COLUMN IF NOT EXISTS organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_kdocs_org ON knowledge_documents (organization_id);

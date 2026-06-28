-- ============================================================
-- ForgeMind — Authentication Schema
-- Migration  : V2__auth_schema.sql
-- Sprint     : 2 / Task 2.1
-- Description: Creates the core auth tables: roles, users,
--              user_roles (join), and refresh_tokens.
--              Seeds default application roles.
-- ============================================================

-- ── Roles ────────────────────────────────────────────────────────────────────

CREATE TABLE roles (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- ── Users ────────────────────────────────────────────────────────────────────

CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

-- ── User ↔ Role (join table) ──────────────────────────────────────────────────

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    CONSTRAINT pk_user_roles        PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role   FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ── Refresh Tokens ───────────────────────────────────────────────────────────

CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_refresh_tokens_hash       UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Indexes ──────────────────────────────────────────────────────────────────

-- users: fast lookup by email (login) and username
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);

-- user_roles: lookups from role side
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

-- refresh_tokens: fast token lookup (validation) and user's token list
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at)
    WHERE revoked = FALSE;   -- partial index: only active tokens

-- ── Seed Data: Default Roles ─────────────────────────────────────────────────

INSERT INTO roles (name, description) VALUES
    ('ROLE_USER',  'Standard authenticated user'),
    ('ROLE_ADMIN', 'Platform administrator with elevated privileges');

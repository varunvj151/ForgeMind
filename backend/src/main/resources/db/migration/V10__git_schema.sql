-- ─────────────────────────────────────────────────────────────────────────────
-- V10 — Git Integration Schema
-- Phase 8: Git repositories, branches, commits, pull requests, and code chunks
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Git Repositories ─────────────────────────────────────────────────────────
-- One row per connected repository, linked to a ForgeMind project.
CREATE TABLE git_repositories
(
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL,
    provider        VARCHAR(30)  NOT NULL, -- GitProviderType enum (GITHUB, GITLAB, BITBUCKET, AZURE_DEVOPS)
    owner           VARCHAR(255) NOT NULL, -- org or user name on the provider
    repo_name       VARCHAR(255) NOT NULL, -- repository name on the provider
    full_name       VARCHAR(512) NOT NULL, -- owner/repo_name
    default_branch  VARCHAR(255)          DEFAULT 'main',
    clone_url       VARCHAR(1024),
    visibility      VARCHAR(20)           DEFAULT 'PRIVATE', -- PUBLIC | PRIVATE | INTERNAL
    primary_language VARCHAR(50),
    description     TEXT,
    -- Incremental sync tracking
    last_sync_at    TIMESTAMPTZ,
    last_commit_sha VARCHAR(40),
    webhook_secret  VARCHAR(255),         -- HMAC-SHA256 secret for webhook validation
    webhook_active  BOOLEAN               DEFAULT FALSE,
    -- Access token is stored encrypted; never expose in API responses
    access_token    TEXT,
    -- Timestamps
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_git_repositories PRIMARY KEY (id)
);

-- Each project can have multiple repositories; provider+owner+name must be unique globally
CREATE UNIQUE INDEX idx_git_repositories_full_name
    ON git_repositories (provider, owner, repo_name);

CREATE INDEX idx_git_repositories_project
    ON git_repositories (project_id);

-- ── Git Branches ─────────────────────────────────────────────────────────────
CREATE TABLE git_branches
(
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    repository_id UUID        NOT NULL,
    name          VARCHAR(255) NOT NULL,
    commit_sha    VARCHAR(40),
    is_default    BOOLEAN              DEFAULT FALSE,
    is_protected  BOOLEAN              DEFAULT FALSE,
    synced_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_git_branches PRIMARY KEY (id),
    CONSTRAINT fk_git_branches_repository
        FOREIGN KEY (repository_id)
            REFERENCES git_repositories (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_git_branches_repo_name
    ON git_branches (repository_id, name);

-- ── Git Commits ───────────────────────────────────────────────────────────────
CREATE TABLE git_commits
(
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    repository_id   UUID         NOT NULL,
    sha             VARCHAR(40)  NOT NULL,
    message         TEXT,
    author_name     VARCHAR(255),
    author_email    VARCHAR(255),
    authored_at     TIMESTAMPTZ,
    branch_name     VARCHAR(255),
    files_changed   INT                   DEFAULT 0,
    additions       INT                   DEFAULT 0,
    deletions       INT                   DEFAULT 0,
    synced_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_git_commits PRIMARY KEY (id),
    CONSTRAINT fk_git_commits_repository
        FOREIGN KEY (repository_id)
            REFERENCES git_repositories (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_git_commits_repo_sha
    ON git_commits (repository_id, sha);

CREATE INDEX idx_git_commits_authored_at
    ON git_commits (repository_id, authored_at DESC);

-- ── Git Pull Requests ─────────────────────────────────────────────────────────
CREATE TABLE git_pull_requests
(
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    repository_id   UUID         NOT NULL,
    pr_number       INT          NOT NULL,
    title           TEXT,
    description     TEXT,
    state           VARCHAR(20)  NOT NULL DEFAULT 'OPEN', -- OPEN | CLOSED | MERGED
    author_login    VARCHAR(255),
    source_branch   VARCHAR(255),
    target_branch   VARCHAR(255),
    files_changed   INT                   DEFAULT 0,
    additions       INT                   DEFAULT 0,
    deletions       INT                   DEFAULT 0,
    merged_at       TIMESTAMPTZ,
    closed_at       TIMESTAMPTZ,
    ai_reviewed     BOOLEAN               DEFAULT FALSE,
    ai_review_result TEXT,                 -- JSON of CodeReviewResult
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_git_pull_requests PRIMARY KEY (id),
    CONSTRAINT fk_git_pull_requests_repository
        FOREIGN KEY (repository_id)
            REFERENCES git_repositories (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_git_pull_requests_repo_number
    ON git_pull_requests (repository_id, pr_number);

CREATE INDEX idx_git_pull_requests_state
    ON git_pull_requests (repository_id, state);

-- ── Code Chunks (Vector Table) ────────────────────────────────────────────────
-- Stores source-file chunks with vector embeddings for semantic code search.
-- The embedding column is managed via native JDBC (same pattern as knowledge_chunks).
CREATE TABLE code_chunks
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    repository_id UUID         NOT NULL,
    project_id    UUID         NOT NULL, -- denormalized for fast project-scoped search
    file_path     VARCHAR(1024) NOT NULL,
    language      VARCHAR(30)  NOT NULL, -- Language enum value
    symbol_name   VARCHAR(500),          -- class/method/function name, if detected
    chunk_index   INT          NOT NULL DEFAULT 0,
    chunk_text    TEXT         NOT NULL,
    embedding     vector(1536),          -- NULL until embedding is generated asynchronously
    metadata      JSONB,                 -- branch, commit_sha, author, etc.
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_code_chunks PRIMARY KEY (id),
    CONSTRAINT fk_code_chunks_repository
        FOREIGN KEY (repository_id)
            REFERENCES git_repositories (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_code_chunks_repository
    ON code_chunks (repository_id);

CREATE INDEX idx_code_chunks_project
    ON code_chunks (project_id);

CREATE INDEX idx_code_chunks_file
    ON code_chunks (repository_id, file_path);

-- IVFFlat ANN index for cosine similarity search over code chunks
CREATE INDEX idx_code_chunks_embedding
    ON code_chunks USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

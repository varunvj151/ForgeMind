-- ─────────────────────────────────────────────────────────────────────────────
-- V9 — RAG Knowledge Base Schema
-- Requires: pgvector extension (available in pgvector/pgvector:pg16 Docker image)
-- ─────────────────────────────────────────────────────────────────────────────

-- Enable pgvector extension (idempotent)
CREATE EXTENSION IF NOT EXISTS vector;

-- ── Knowledge Documents ───────────────────────────────────────────────────────
-- One record per indexed source object (project, task, team, activity, documentation).
-- Tracks content checksum to skip re-indexing unchanged content.
CREATE TABLE knowledge_documents
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    source_type  VARCHAR(50)  NOT NULL, -- KnowledgeSourceType enum value
    source_id    VARCHAR(255) NOT NULL, -- UUID of the originating entity (stored as text)
    project_id   UUID         NOT NULL, -- for access-control scoping
    title        VARCHAR(500),
    checksum     VARCHAR(64),           -- SHA-256 hex of indexed content; skip re-embed if unchanged
    chunk_count  INT          NOT NULL DEFAULT 0,
    indexed_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_knowledge_documents PRIMARY KEY (id)
);

-- Enforce uniqueness per source object (no duplicate document per entity)
CREATE UNIQUE INDEX idx_knowledge_documents_source
    ON knowledge_documents (source_type, source_id);

-- Fast project-scoped document lookups and cascade deletes
CREATE INDEX idx_knowledge_documents_project
    ON knowledge_documents (project_id);

-- ── Knowledge Chunks ─────────────────────────────────────────────────────────
-- Each document is split into overlapping text chunks.
-- Embeddings are written asynchronously after chunk creation.
CREATE TABLE knowledge_chunks
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    document_id UUID         NOT NULL,
    project_id  UUID         NOT NULL, -- denormalized: avoids join on every vector search
    source_type VARCHAR(50)  NOT NULL,
    source_id   VARCHAR(255) NOT NULL,
    chunk_index INT          NOT NULL, -- 0-based position within the document
    chunk_text  TEXT         NOT NULL,
    embedding   vector(1536),          -- NULL until embedding is generated asynchronously
    metadata    JSONB,                 -- arbitrary key-value: title, status, priority, assignee ...
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_knowledge_chunks PRIMARY KEY (id),
    CONSTRAINT fk_knowledge_chunks_document
        FOREIGN KEY (document_id)
            REFERENCES knowledge_documents (id)
            ON DELETE CASCADE
);

-- Fast chunk retrieval for a document (e.g. re-embedding, deletion)
CREATE INDEX idx_knowledge_chunks_document
    ON knowledge_chunks (document_id);

-- Project-scoped chunk lookups (used in authorization checks + batch ops)
CREATE INDEX idx_knowledge_chunks_project
    ON knowledge_chunks (project_id);

-- IVFFlat approximate nearest-neighbour index for cosine similarity search.
-- lists=100 is a reasonable default; re-index with higher lists as dataset grows.
-- IMPORTANT: index only becomes useful once ~1000+ rows have embeddings.
CREATE INDEX idx_knowledge_chunks_embedding
    ON knowledge_chunks USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

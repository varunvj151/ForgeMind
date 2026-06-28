-- ============================================================
-- ForgeMind — Project Ownership
-- Migration  : V5__project_ownership.sql
-- Description: Adds owner_id FK to projects table, linking
--              each project to its creator (a user).
-- ============================================================

ALTER TABLE projects
    ADD COLUMN owner_id BIGINT NOT NULL DEFAULT 0;

-- Remove the temporary DEFAULT now that the column exists
-- (all existing rows get 0; in dev this is fine; prod starts empty)
ALTER TABLE projects
    ALTER COLUMN owner_id DROP DEFAULT;

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX idx_projects_owner_id ON projects (owner_id);

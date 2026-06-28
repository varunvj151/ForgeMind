-- ============================================================
-- ForgeMind — Activity Feed Schema
-- Migration  : V8__activity_schema.sql
-- Description: Creates immutable activities table for audit logging.
--              Activities are never updated after creation.
-- ============================================================

CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    metadata TEXT,
    project_id UUID,
    team_id UUID,
    task_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_activities_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Chronological queries per project (primary access pattern for project timelines)
CREATE INDEX idx_activities_project_id_created_at ON activities (project_id, created_at DESC);

-- Chronological queries per team
CREATE INDEX idx_activities_team_id_created_at ON activities (team_id, created_at DESC);

-- User activity history
CREATE INDEX idx_activities_actor_id_created_at ON activities (actor_id, created_at DESC);

-- Note: task_id and project_id/team_id are intentionally NOT FK-constrained
-- so that activities survive if the referenced task/team/project is soft-deleted
-- or if we later want to add archival. The actor_id FK is kept because
-- an activity without an actor is meaningless.

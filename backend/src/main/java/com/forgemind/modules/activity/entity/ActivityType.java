package com.forgemind.modules.activity.entity;

/**
 * Enumeration of all business events that can be recorded in the activity feed.
 * Add new types here as new modules are introduced.
 * Never remove or rename existing types — doing so would break historical records.
 */
public enum ActivityType {

    // ── Project events ────────────────────────────────────────────────────────
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_DELETED,

    // ── Team events ───────────────────────────────────────────────────────────
    TEAM_CREATED,
    TEAM_MEMBER_ADDED,
    TEAM_MEMBER_REMOVED,

    // ── Task events ───────────────────────────────────────────────────────────
    TASK_CREATED,
    TASK_UPDATED,
    TASK_ASSIGNED,
    TASK_STATUS_CHANGED,
    TASK_COMPLETED,
    TASK_DELETED,

    // ── User events ───────────────────────────────────────────────────────────
    USER_UPDATED
}

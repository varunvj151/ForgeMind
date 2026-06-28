package com.forgemind.modules.project.entity;

/**
 * Lifecycle status of a Project.
 *
 * <p>Persisted as a VARCHAR string via {@code @Enumerated(EnumType.STRING)}
 * so that the database column is human-readable and schema-stable.
 */
public enum ProjectStatus {

    /** Project is currently active and being worked on. */
    ACTIVE,

    /** Project has been completed successfully. */
    COMPLETED,

    /** Project has been archived and is no longer active. */
    ARCHIVED
}

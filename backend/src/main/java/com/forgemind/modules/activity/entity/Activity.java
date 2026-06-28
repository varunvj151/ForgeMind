package com.forgemind.modules.activity.entity;

import com.forgemind.modules.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit record of a business event on the platform.
 *
 * <p>Activities are NEVER updated after creation. All columns are marked
 * {@code updatable = false}. There is no {@code @PreUpdate} hook.
 *
 * <p>Foreign-key references to project/team/task are stored as bare UUIDs
 * (not @ManyToOne) so that activities survive entity deletion and serve
 * as a durable audit trail.
 */
@Entity
@Table(name = "activities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false, updatable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50, updatable = false)
    private ActivityType activityType;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String message;

    /**
     * Structured metadata stored as a JSON string.
     * Contains context-specific key/value pairs (e.g., old/new status, assignee name).
     */
    @Column(name = "metadata", columnDefinition = "TEXT", updatable = false)
    private String metadata;

    // ── Nullable context references (bare IDs, not JPA associations) ──────────

    @Column(name = "project_id", updatable = false)
    private UUID projectId;

    @Column(name = "team_id", updatable = false)
    private UUID teamId;

    @Column(name = "task_id", updatable = false)
    private UUID taskId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

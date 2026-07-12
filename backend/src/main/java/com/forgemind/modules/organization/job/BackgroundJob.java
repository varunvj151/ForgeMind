package com.forgemind.modules.organization.job;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Represents an asynchronous background task scoped to an organization. */
@Entity
@Table(name = "background_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundJob {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "job_type", nullable = false, length = 100)
  private String jobType;

  @Column(name = "payload", columnDefinition = "JSONB")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  private JobStatus status = JobStatus.PENDING;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}

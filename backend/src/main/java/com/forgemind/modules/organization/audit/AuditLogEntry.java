package com.forgemind.modules.organization.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Immutable audit log entry — never updated after insert. */
@Entity
@Table(name = "audit_log_entries")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "organization_id")
  private UUID organizationId;

  @Column(name = "actor_id")
  private Long actorId;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  @Column(name = "resource_type", length = 100)
  private String resourceType;

  @Column(name = "resource_id", length = 255)
  private String resourceId;

  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}

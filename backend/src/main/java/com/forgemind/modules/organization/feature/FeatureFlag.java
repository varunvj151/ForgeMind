package com.forgemind.modules.organization.feature;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Per-organization override for a specific feature flag. */
@Entity
@Table(name = "feature_flags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "flag_name"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Enumerated(EnumType.STRING)
  @Column(name = "flag_name", nullable = false, length = 100)
  private PlanFeature flagName;

  @Column(name = "enabled", nullable = false)
  @Builder.Default
  private boolean enabled = false;

  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}

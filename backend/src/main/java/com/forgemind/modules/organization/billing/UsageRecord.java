package com.forgemind.modules.organization.billing;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

/** Tracks organization usage for a specific metric on a specific date. */
@Entity
@Table(name = "usage_records",
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "metric", "recorded_date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Enumerated(EnumType.STRING)
  @Column(name = "metric", nullable = false, length = 50)
  private UsageMetric metric;

  @Column(name = "value", nullable = false)
  @Builder.Default
  private long value = 0;

  @Column(name = "recorded_date", nullable = false)
  @Builder.Default
  private LocalDate recordedDate = LocalDate.now();

  @Column(name = "created_at", nullable = false, updatable = false)
  private java.time.Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = java.time.Instant.now();
  }
}

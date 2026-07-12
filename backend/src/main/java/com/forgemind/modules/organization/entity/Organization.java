package com.forgemind.modules.organization.entity;

import com.forgemind.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Root aggregate representing a tenant (organization) in the ForgeMind SaaS platform. */
@Entity
@Table(name = "organizations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  /** URL-friendly unique identifier, e.g. "acme-corp". */
  @Column(name = "slug", nullable = false, unique = true, length = 100)
  private String slug;

  @Column(name = "logo_url", length = 500)
  private String logoUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "plan", nullable = false, length = 30)
  @Builder.Default
  private OrganizationPlan plan = OrganizationPlan.FREE;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private OrganizationStatus status = OrganizationStatus.ACTIVE;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}

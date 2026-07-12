package com.forgemind.modules.organization.entity;

import com.forgemind.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Join entity linking a {@link User} to an {@link Organization} with a specific role. */
@Entity
@Table(
    name = "organization_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMember {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  @Builder.Default
  private OrganizationMemberRole role = OrganizationMemberRole.MEMBER;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @PrePersist
  protected void onCreate() {
    if (joinedAt == null) joinedAt = Instant.now();
  }
}

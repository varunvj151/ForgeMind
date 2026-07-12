package com.forgemind.modules.organization.entity;

import com.forgemind.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Invitation sent to a user's email to join an organization. */
@Entity
@Table(name = "organization_invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationInvitation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  /** SHA-256 hash of the raw invitation token (never store plaintext). */
  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  @Builder.Default
  private OrganizationMemberRole role = OrganizationMemberRole.MEMBER;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private InvitationStatus status = InvitationStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_by")
  private User invitedBy;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }

  /** Returns true if this invitation is still usable. */
  public boolean isValid() {
    return status == InvitationStatus.PENDING && Instant.now().isBefore(expiresAt);
  }
}

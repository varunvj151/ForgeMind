package com.forgemind.modules.organization.sso;

import com.forgemind.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Links a ForgeMind user to an identity provided by an SSO provider. */
@Entity
@Table(name = "sso_identities",
    uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id", "external_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoIdentity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "provider_id", nullable = false)
  private SsoProvider provider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "external_id", nullable = false, length = 255)
  private String externalId; // e.g., the sub claim from OIDC

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}

package com.forgemind.modules.organization.sso;

import com.forgemind.modules.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/** Configuration for an SSO provider (SAML/OIDC) attached to an organization. */
@Entity
@Table(name = "sso_providers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(name = "provider_type", nullable = false, length = 50)
  private String providerType; // e.g., "SAML", "OIDC", "GOOGLE_WORKSPACE"

  @Column(name = "domain", nullable = false, length = 255)
  private String domain; // e.g., "acme.com"

  @Column(name = "issuer_url", length = 500)
  private String issuerUrl;

  @Column(name = "client_id", length = 255)
  private String clientId;

  @Column(name = "client_secret", length = 255)
  private String clientSecret; // Encrypted in real life

  @Column(name = "active", nullable = false)
  @Builder.Default
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}

package com.forgemind.modules.organization.apikey;

import com.forgemind.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Represents a hashed API key used for programmatic access to the ForgeMind API. */
@Entity
@Table(name = "api_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  @Builder.Default
  private ApiKeyType type = ApiKeyType.PERSONAL;

  /** SHA-256 hex of the raw token. Never store plaintext. */
  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "scopes", columnDefinition = "TEXT[]")
  private List<String> scopes;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "revoked", nullable = false)
  @Builder.Default
  private boolean revoked = false;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }

  /** Returns true if the key is active and not expired. */
  public boolean isActive() {
    if (revoked) return false;
    return expiresAt == null || Instant.now().isBefore(expiresAt);
  }
}

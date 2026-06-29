package com.forgemind.modules.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistent refresh-token record — maps to the {@code refresh_tokens} table.
 *
 * <p>The actual token string is stored as a SHA-256 hash ({@code token_hash}) so that even if the
 * database is compromised, raw tokens cannot be replayed without knowing the original value.
 *
 * <p>Tokens are soft-deleted via the {@code revoked} flag rather than hard-deleted, allowing audit
 * trails. A scheduled job should periodically purge old rows.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Owner of this token. Cascade-deleted when the user is removed. */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * SHA-256 hash of the opaque refresh token string sent to the client. Stored uniquely — each
   * issued token results in exactly one row.
   */
  @Column(name = "token_hash", nullable = false, unique = true, length = 255)
  private String tokenHash;

  /** When this token expires. Compared against {@code Instant.now()} on every use. */
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  /** {@code true} when the token has been explicitly invalidated (logout or rotation). */
  @Column(name = "revoked", nullable = false)
  @Builder.Default
  private boolean revoked = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  /** Convenience check: token is usable when it is neither expired nor revoked. */
  public boolean isValid() {
    return !revoked && Instant.now().isBefore(expiresAt);
  }
}

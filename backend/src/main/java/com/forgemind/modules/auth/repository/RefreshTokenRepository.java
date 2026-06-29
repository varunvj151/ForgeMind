package com.forgemind.modules.auth.repository;

import com.forgemind.modules.auth.entity.RefreshToken;
import com.forgemind.modules.auth.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link RefreshToken} entities.
 *
 * <p>Core access patterns:
 *
 * <ul>
 *   <li>Lookup token by hash for validation during token refresh
 *   <li>Revoke all tokens for a user on logout
 *   <li>Purge expired tokens (scheduled maintenance)
 * </ul>
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Finds an active (non-revoked) token by its hash. Used during the refresh-token flow to validate
   * the supplied token.
   *
   * @param tokenHash SHA-256 hash of the raw token string
   * @return an {@link Optional} containing the token record if found
   */
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * Returns all refresh tokens owned by a given user. Useful for listing or bulk-revoking a user's
   * sessions.
   *
   * @param user the owning user
   * @return list of token records (may be empty)
   */
  List<RefreshToken> findByUser(User user);

  /**
   * Revokes (soft-deletes) all non-revoked tokens belonging to the given user. Called on logout to
   * invalidate all active sessions.
   *
   * @param user the user whose tokens should be revoked
   */
  @Modifying
  @Query(
      "UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
  void revokeAllByUser(@Param("user") User user);

  /**
   * Deletes all expired tokens from the database. Intended to be called by a scheduled maintenance
   * job.
   *
   * @param now tokens with {@code expiresAt} before this instant are deleted
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  void deleteAllExpiredBefore(@Param("now") Instant now);
}

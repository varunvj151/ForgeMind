package com.forgemind.modules.auth.repository;

import com.forgemind.modules.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link User} entities.
 *
 * <p>Core access patterns:
 *
 * <ul>
 *   <li>Login: look up by {@code email} (primary login field)
 *   <li>JWT filter: look up by {@code username} (stored as JWT subject)
 *   <li>Registration guard: existence checks by email and username
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by their unique username. Used by {@link
   * org.springframework.security.core.userdetails.UserDetailsService} and the JWT authentication
   * filter.
   *
   * @param username the username to search for
   * @return an {@link Optional} containing the user if found
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by their unique email address. Used during login when the client supplies an email
   * instead of username.
   *
   * @param email the email address to search for
   * @return an {@link Optional} containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Returns {@code true} if a user with the given username already exists. Used during registration
   * to prevent duplicate usernames.
   *
   * @param username the username to check
   * @return true if taken
   */
  boolean existsByUsername(String username);

  /**
   * Returns {@code true} if a user with the given email already exists. Used during registration to
   * prevent duplicate accounts.
   *
   * @param email the email to check
   * @return true if taken
   */
  boolean existsByEmail(String email);
}

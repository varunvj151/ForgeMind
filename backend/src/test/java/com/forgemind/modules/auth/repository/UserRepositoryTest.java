package com.forgemind.modules.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.entity.User;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository-layer tests for {@link UserRepository}.
 *
 * <p>Uses {@code @DataJpaTest}: H2 in-memory DB, transactional rollback per test, JPA-only context.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository")
class UserRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    Role userRole =
        roleRepository.save(Role.builder().name("ROLE_USER").description("Standard user").build());

    User user =
        User.builder()
            .firstName("John")
            .lastName("Doe")
            .username("john_doe")
            .email("john@example.com")
            .passwordHash("$2a$12$hashedpassword")
            .enabled(true)
            .roles(Set.of(userRole))
            .build();
    savedUser = userRepository.save(user);
  }

  // ── findByUsername ────────────────────────────────────────────────────────

  @Test
  @DisplayName("findByUsername — returns user when username matches")
  void findByUsername_existingUsername_returnsUser() {
    Optional<User> found = userRepository.findByUsername("john_doe");

    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("john_doe");
    assertThat(found.get().getEmail()).isEqualTo("john@example.com");
  }

  @Test
  @DisplayName("findByUsername — returns empty for unknown username")
  void findByUsername_unknownUsername_returnsEmpty() {
    Optional<User> found = userRepository.findByUsername("ghost_user");

    assertThat(found).isEmpty();
  }

  // ── findByEmail ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("findByEmail — returns user when email matches")
  void findByEmail_existingEmail_returnsUser() {
    Optional<User> found = userRepository.findByEmail("john@example.com");

    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("john@example.com");
  }

  @Test
  @DisplayName("findByEmail — returns empty for unknown email")
  void findByEmail_unknownEmail_returnsEmpty() {
    Optional<User> found = userRepository.findByEmail("nobody@example.com");

    assertThat(found).isEmpty();
  }

  // ── existsByUsername ──────────────────────────────────────────────────────

  @Test
  @DisplayName("existsByUsername — true for existing username")
  void existsByUsername_existingUsername_returnsTrue() {
    assertThat(userRepository.existsByUsername("john_doe")).isTrue();
  }

  @Test
  @DisplayName("existsByUsername — false for unknown username")
  void existsByUsername_unknownUsername_returnsFalse() {
    assertThat(userRepository.existsByUsername("ghost_user")).isFalse();
  }

  // ── existsByEmail ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("existsByEmail — true for existing email")
  void existsByEmail_existingEmail_returnsTrue() {
    assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
  }

  @Test
  @DisplayName("existsByEmail — false for unknown email")
  void existsByEmail_unknownEmail_returnsFalse() {
    assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
  }

  // ── save & roles ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("save — persists user with generated ID and timestamps")
  void save_newUser_persistsWithIdAndTimestamps() {
    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getCreatedAt()).isNotNull();
    assertThat(savedUser.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("save — roles are persisted and retrievable via EAGER fetch")
  void save_userWithRole_rolesAreEagerlyFetched() {
    User found = userRepository.findByUsername("john_doe").orElseThrow();

    assertThat(found.getRoles()).isNotEmpty();
    assertThat(found.getRoles()).extracting("name").containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("getAuthorities — mapped from roles")
  void getAuthorities_userWithRole_returnsGrantedAuthorities() {
    User found = userRepository.findByUsername("john_doe").orElseThrow();

    assertThat(found.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("isEnabled — true for active user")
  void isEnabled_activeUser_returnsTrue() {
    User found = userRepository.findByUsername("john_doe").orElseThrow();
    assertThat(found.isEnabled()).isTrue();
  }
}

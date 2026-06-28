package com.forgemind.modules.auth.repository;

import com.forgemind.modules.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer tests for {@link RoleRepository}.
 *
 * <p>Uses {@code @DataJpaTest} which:
 * <ul>
 *   <li>Auto-configures an H2 in-memory database</li>
 *   <li>Runs each test in a transaction that is rolled back on completion</li>
 *   <li>Only loads JPA-related beans — no web or security context</li>
 * </ul>
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RoleRepository")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role savedRole;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .name("ROLE_USER")
                .description("Standard authenticated user")
                .build();
        savedRole = roleRepository.save(role);
    }

    @Test
    @DisplayName("findByName — returns role when name matches")
    void findByName_existingName_returnsRole() {
        Optional<Role> found = roleRepository.findByName("ROLE_USER");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_USER");
        assertThat(found.get().getDescription()).isEqualTo("Standard authenticated user");
    }

    @Test
    @DisplayName("findByName — returns empty when name does not exist")
    void findByName_unknownName_returnsEmpty() {
        Optional<Role> found = roleRepository.findByName("ROLE_NONEXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByName — returns true for persisted role")
    void existsByName_existingName_returnsTrue() {
        assertThat(roleRepository.existsByName("ROLE_USER")).isTrue();
    }

    @Test
    @DisplayName("existsByName — returns false for unknown role")
    void existsByName_unknownName_returnsFalse() {
        assertThat(roleRepository.existsByName("ROLE_UNKNOWN")).isFalse();
    }

    @Test
    @DisplayName("save — persists role with generated ID")
    void save_newRole_persistsWithId() {
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .description("Platform administrator")
                .build();

        Role persisted = roleRepository.save(adminRole);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findAll — returns all seeded roles")
    void findAll_afterSetup_returnsAtLeastOne() {
        assertThat(roleRepository.findAll()).isNotEmpty();
    }
}

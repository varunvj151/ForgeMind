package com.forgemind.modules.auth.repository;

import com.forgemind.modules.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Role} entities.
 *
 * <p>In normal application flow, roles are read-only (seeded by Flyway).
 * This repository is used during user registration to look up role records
 * by their authority string name.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its Spring Security authority string, e.g. {@code "ROLE_USER"}.
     *
     * @param name the exact role name
     * @return an {@link Optional} containing the role if it exists
     */
    Optional<Role> findByName(String name);

    /**
     * Returns {@code true} if a role with the given name exists.
     *
     * @param name the exact role name
     * @return true if the role exists
     */
    boolean existsByName(String name);
}

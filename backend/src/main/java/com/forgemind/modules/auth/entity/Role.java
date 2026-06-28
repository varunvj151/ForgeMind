package com.forgemind.modules.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an application role (e.g., ROLE_USER, ROLE_ADMIN).
 *
 * <p>Roles are seeded by the V2 Flyway migration. They are never created
 * at runtime via the API — only assigned to users during registration or
 * by an admin.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role authority string, e.g. {@code ROLE_USER} or {@code ROLE_ADMIN}.
     * The {@code ROLE_} prefix is required by Spring Security's
     * {@link org.springframework.security.core.authority.SimpleGrantedAuthority}.
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

package com.forgemind.modules.project.dto.response;

import com.forgemind.modules.project.entity.ProjectStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO representing a Project.
 *
 * <p>Never exposes the JPA entity directly. Controllers return this record.
 */
public record ProjectResponse(
    UUID id,
    String name,
    String description,
    ProjectStatus status,
    Instant createdAt,
    Instant updatedAt,
    Long ownerId,
    String ownerUsername) {}

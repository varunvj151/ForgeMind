package com.forgemind.modules.project.dto.request;

import com.forgemind.modules.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound DTO for creating or updating a Project.
 *
 * <p>All Bean Validation constraints live here — never validated manually inside controllers or
 * services.
 */
public record ProjectRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,
    @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,
    @NotNull(message = "Status is required") ProjectStatus status) {}

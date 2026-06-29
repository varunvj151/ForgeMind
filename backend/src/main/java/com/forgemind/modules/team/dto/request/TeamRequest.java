package com.forgemind.modules.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
    @NotBlank(message = "Team name is required")
        @Size(max = 255, message = "Team name must not exceed 255 characters")
        String name,
    String description) {}

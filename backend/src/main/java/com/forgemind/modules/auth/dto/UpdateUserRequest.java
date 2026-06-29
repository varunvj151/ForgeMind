package com.forgemind.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName) {}

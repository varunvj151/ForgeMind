package com.forgemind.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/auth/login}.
 *
 * <p>The {@code usernameOrEmail} field accepts either a registered username or the email address
 * associated with the account. The service resolves the appropriate {@link
 * com.forgemind.modules.auth.entity.User} before delegating credential verification to Spring
 * Security's {@link org.springframework.security.authentication.AuthenticationManager}.
 *
 * <p>Intentionally vague field name — the client need not know whether the backend performs a
 * username lookup or an email lookup first.
 */
@Schema(description = "Login credentials. Supply either the registered username or email address.")
public record LoginRequest(
    @NotBlank(message = "Username or email is required")
        @Schema(description = "Registered username or email address", example = "alice@example.com")
        String usernameOrEmail,
    @NotBlank(message = "Password is required")
        @Schema(
            description = "Account password (plaintext — sent over HTTPS)",
            example = "securepassword123")
        String password) {}

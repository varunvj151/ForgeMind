package com.forgemind.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body for a successful {@code POST /api/v1/auth/login}.
 *
 * <p>Contains both JWT tokens and the authenticated user's public profile. Passwords and password
 * hashes are never included.
 *
 * <p>The {@code refreshToken} is an opaque random value (UUID). The server stores only its SHA-256
 * hash, so the raw token is unrecoverable from the database even if it is compromised.
 *
 * @param accessToken Short-lived HS256 JWT — include as {@code Authorization: Bearer <token>}
 * @param refreshToken Opaque long-lived token — used to obtain a new access token
 * @param tokenType Always {@code "Bearer"}
 * @param expiresIn Access token lifetime in seconds (e.g. 900 for 15 min)
 * @param user Public profile of the authenticated user
 */
@Schema(description = "Successful login response containing JWT tokens and user profile.")
public record LoginResponse(
    @Schema(
            description = "Short-lived JWT access token (HS256).",
            example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
    @Schema(description = "Opaque long-lived refresh token.", example = "a1b2c3d4-e5f6-...")
        String refreshToken,
    @Schema(description = "Token scheme — always 'Bearer'.", example = "Bearer") String tokenType,
    @Schema(description = "Access token lifetime in seconds.", example = "900") long expiresIn,
    @Schema(description = "Public profile of the authenticated user.") UserResponse user) {}

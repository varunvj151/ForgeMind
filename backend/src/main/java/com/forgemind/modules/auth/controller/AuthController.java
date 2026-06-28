package com.forgemind.modules.auth.controller;

import com.forgemind.common.dto.ErrorResponse;
import com.forgemind.modules.auth.dto.LoginRequest;
import com.forgemind.modules.auth.dto.LoginResponse;
import com.forgemind.modules.auth.dto.RegisterRequest;
import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller handling public auth endpoints.
 *
 * <p>All endpoints under {@code /api/v1/auth/**} are publicly accessible
 * (no JWT token required) as configured in {@link com.forgemind.config.SecurityConfig}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {

    private final AuthService authService;

    // ── Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a new user account.
     *
     * <p>The password is hashed with BCrypt before storage. The plaintext password
     * is never logged or stored. On success, the new user is assigned {@code ROLE_USER}.
     *
     * @param request the registration request body
     * @return {@code 201 Created} with the created user's public profile
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with ROLE_USER. Password is hashed with BCrypt. " +
                    "Returns the created user's public profile (never exposes password or hash)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error (invalid email, short password, etc.)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and issues JWT access + refresh tokens.
     *
     * <p>Accepts either a registered username or an email address in the
     * {@code usernameOrEmail} field. On success returns a short-lived access token
     * (default 15 min / {@code app.jwt.expiration-ms}) and a long-lived refresh
     * token (default 7 days / {@code app.jwt.refresh-expiration-ms}).
     *
     * <p>Authentication failures always return {@code 401} with code
     * {@code INVALID_CREDENTIALS} — the response never reveals whether the
     * identifier or the password was wrong.
     *
     * @param request the login credentials
     * @return {@code 200 OK} with both tokens and the user's public profile
     */
    @Operation(
            summary = "Login — obtain JWT tokens",
            description = "Authenticate with username-or-email and password. " +
                    "Returns an access token (short-lived) and a refresh token (long-lived). " +
                    "Include the access token as `Authorization: Bearer <token>` on subsequent requests."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful — tokens issued",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                            {
                                              "accessToken":  "eyJhbGciOiJIUzI1NiJ9...",
                                              "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                              "tokenType":    "Bearer",
                                              "expiresIn":    900,
                                              "user": {
                                                "id":        1,
                                                "username":  "alicesmith",
                                                "email":     "alice@example.com",
                                                "firstName": "Alice",
                                                "lastName":  "Smith",
                                                "role":      ["ROLE_USER"],
                                                "createdAt": "2026-06-28T12:00:00Z"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error (blank usernameOrEmail or password)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Blank field",
                                    value = """
                                            {
                                              "error": {
                                                "code":    "VALIDATION_ERROR",
                                                "message": "Request validation failed",
                                                "status":  400
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials — username/email or password is incorrect, " +
                            "or the account is disabled",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Invalid credentials",
                                    value = """
                                            {
                                              "error": {
                                                "code":    "INVALID_CREDENTIALS",
                                                "message": "Invalid username/email or password",
                                                "status":  401
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }
}

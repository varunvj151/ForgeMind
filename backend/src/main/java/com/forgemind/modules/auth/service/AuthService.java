package com.forgemind.modules.auth.service;

import com.forgemind.common.exception.ForgemindException;
import com.forgemind.config.JwtProperties;
import com.forgemind.modules.auth.dto.LoginRequest;
import com.forgemind.modules.auth.dto.LoginResponse;
import com.forgemind.modules.auth.dto.RegisterRequest;
import com.forgemind.modules.auth.dto.UserResponse;
import com.forgemind.modules.auth.entity.RefreshToken;
import com.forgemind.modules.auth.entity.Role;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.RefreshTokenRepository;
import com.forgemind.modules.auth.repository.RoleRepository;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final RoleRepository         roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final JwtService             jwtService;
    private final JwtProperties          jwtProperties;

    // ── Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a new user.
     *
     * @param request the registration request containing user details
     * @return {@link UserResponse} with the newly created user's public profile
     */
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        String username  = request.username().trim();
        String email     = request.email().trim().toLowerCase();
        String firstName = request.firstName().trim();
        String lastName  = request.lastName().trim();

        log.debug("Attempting to register new user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new ForgemindException(
                    "USERNAME_EXISTS",
                    "Username is already taken",
                    HttpStatus.CONFLICT
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ForgemindException(
                    "EMAIL_EXISTS",
                    "Email is already taken",
                    HttpStatus.CONFLICT
            );
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ForgemindException(
                        "ROLE_NOT_FOUND",
                        "Default user role not found in the database",
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("Successfully registered user: {} with ID: {}", savedUser.getUsername(), savedUser.getId());

        return UserResponse.fromUser(savedUser);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user by username-or-email and password, then issues JWT tokens.
     *
     * <p>Steps:
     * <ol>
     *   <li>Resolve the {@link User} by username or email — throws {@code 401} if not found.</li>
     *   <li>Delegate credential verification to {@link AuthenticationManager} (BCrypt check +
     *       account status checks handled by Spring Security automatically).</li>
     *   <li>On success: generate access token and refresh token via {@link JwtService}.</li>
     *   <li>Delete all expired refresh tokens for the user, then persist the new one.</li>
     * </ol>
     *
     * <p>On any authentication failure the error code is always {@code INVALID_CREDENTIALS}
     * — the response never reveals whether the identifier or the password was wrong.
     *
     * @param request the login request
     * @return {@link LoginResponse} containing both tokens and the user's public profile
     */
    @Transactional
    public LoginResponse loginUser(LoginRequest request) {
        String identifier = request.usernameOrEmail().trim();

        // 1. Resolve user by username first, then by email
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> new ForgemindException(
                        "INVALID_CREDENTIALS",
                        "Invalid username/email or password",
                        HttpStatus.UNAUTHORIZED
                ));

        // 2. Delegate authentication (BCrypt verification + enabled/locked checks)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
            );
        } catch (AuthenticationException ex) {
            log.debug("Authentication failure for identifier '{}': {}", identifier, ex.getClass().getSimpleName());
            throw new ForgemindException(
                    "INVALID_CREDENTIALS",
                    "Invalid username/email or password",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 3. Generate tokens
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. Purge expired refresh tokens, then persist the new one
        refreshTokenRepository.deleteAllExpiredBefore(Instant.now());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User '{}' (ID: {}) logged in successfully", user.getUsername(), user.getId());

        long expiresInSeconds = jwtProperties.getExpirationMs() / 1000;

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresInSeconds,
                UserResponse.fromUser(user)
        );
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Returns the SHA-256 hex digest of the given token string.
     * Only the hash is stored in the database, so raw tokens cannot be recovered
     * even if the table is compromised.
     *
     * @param token the raw opaque token
     * @return lowercase hex-encoded SHA-256 hash
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JVM spec — this can never happen
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

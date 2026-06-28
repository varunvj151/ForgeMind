package com.forgemind.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Typed binding for the {@code app.jwt.*} section in {@code application.yml}.
 *
 * <p>Validated at startup so that a missing or blank secret causes a descriptive
 * startup failure rather than a cryptic signature error at runtime.
 *
 * <pre>
 * app:
 *   jwt:
 *     secret: ${JWT_SECRET:changeme_this_is_insecure_replace_in_production}
 *     expiration-ms: ${JWT_EXPIRATION_MS:3600000}
 *     refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
 * </pre>
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * HS256 signing secret. Must be at least 256 bits (32 ASCII characters) for HS256.
     * Override via {@code JWT_SECRET} environment variable in production.
     */
    @NotBlank(message = "app.jwt.secret must not be blank")
    private String secret;

    /**
     * Access token expiry in milliseconds. Default: 1 hour (3,600,000 ms).
     */
    @Min(value = 60_000, message = "app.jwt.expiration-ms must be at least 60,000 ms (1 minute)")
    private long expirationMs;

    /**
     * Refresh token expiry in milliseconds. Default: 7 days (604,800,000 ms).
     */
    @Min(value = 60_000, message = "app.jwt.refresh-expiration-ms must be at least 60,000 ms (1 minute)")
    private long refreshExpirationMs;
}

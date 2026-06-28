package com.forgemind.modules.auth.security;

import com.forgemind.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for creating and validating HS256-signed JWTs.
 *
 * <p>Algorithm: HMAC-SHA256 (HS256) — symmetric, single-secret scheme.
 * The secret is loaded from {@code app.jwt.secret} via {@link JwtProperties}.
 *
 * <p>Token structure:
 * <pre>
 * Header : { "alg": "HS256", "typ": "JWT" }
 * Payload: { "sub": "&lt;username&gt;", "iat": &lt;epoch&gt;, "exp": &lt;epoch&gt; }
 * </pre>
 *
 * <p>The subject ({@code sub}) claim stores the username, which is used by
 * {@link JwtAuthenticationFilter} to reload the {@link UserDetails} from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ── Token Generation ─────────────────────────────────────────────────────

    /**
     * Generates an access token for the given user with no extra claims.
     *
     * @param userDetails the authenticated user
     * @return signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates an access token with additional custom claims merged into the payload.
     *
     * @param extraClaims map of additional claims (e.g., roles, user ID)
     * @param userDetails the authenticated user
     * @return signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtProperties.getExpirationMs());
    }

    /**
     * Generates a long-lived refresh JWT for the given user.
     *
     * <p>The refresh token uses the same HS256 signing key as the access token but
     * carries a much longer expiry driven by {@code app.jwt.refresh-expiration-ms}.
     * It is stored (as a SHA-256 hash of the opaque random value) in the
     * {@code refresh_tokens} table and used only to issue new access tokens.
     *
     * @param userDetails the authenticated user
     * @return signed JWT refresh token string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshExpirationMs());
    }

    // ── Token Validation ─────────────────────────────────────────────────────

    /**
     * Validates a token against the supplied {@link UserDetails}.
     *
     * <p>A token is valid when:
     * <ol>
     *   <li>Its signature matches (verified internally by JJWT)</li>
     *   <li>It has not expired</li>
     *   <li>The {@code sub} claim matches the username in {@code userDetails}</li>
     * </ol>
     *
     * @param token       the raw JWT string
     * @param userDetails the user to validate against
     * @return {@code true} if the token is valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Returns {@code true} if the token's expiry timestamp is in the past.
     *
     * @param token the raw JWT string
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claim Extraction ─────────────────────────────────────────────────────

    /**
     * Extracts the {@code sub} (subject / username) claim from the token.
     *
     * @param token the raw JWT string
     * @return the username stored in the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the {@code exp} (expiration) claim from the token.
     *
     * @param token the raw JWT string
     * @return the expiry {@link Date}
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor using a resolver function.
     *
     * @param token          the raw JWT string
     * @param claimsResolver a function mapping {@link Claims} to the desired value
     * @param <T>            the return type
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Derives the {@link SecretKey} from the base64-encoded secret string in properties.
     * The key is used for both signing new tokens and verifying incoming ones.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(
                        jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)
                )
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

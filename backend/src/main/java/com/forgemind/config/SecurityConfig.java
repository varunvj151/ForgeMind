package com.forgemind.config;

import com.forgemind.modules.auth.security.JwtAuthenticationEntryPoint;
import com.forgemind.modules.auth.security.JwtAuthenticationFilter;
import com.forgemind.modules.auth.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration — Phase 2 (JWT-secured).
 *
 * <p>Replaces the Phase-1 permissive baseline ({@code anyRequest().permitAll()}) with
 * a stateless JWT filter chain. All endpoints require a valid Bearer token except
 * the explicitly whitelisted public paths below.
 *
 * <p>Public endpoints (no token required):
 * <ul>
 *   <li>{@code /api/v1/auth/**}   — login, registration, token refresh</li>
 *   <li>{@code /actuator/health}  — readiness probe (LoadBalancer / k8s)</li>
 *   <li>{@code /v3/api-docs/**}   — OpenAPI spec (Springdoc default path)</li>
 *   <li>{@code /api-docs/**}      — OpenAPI spec (custom path in application.yml)</li>
 *   <li>{@code /swagger-ui/**}    — Swagger UI static assets</li>
 *   <li>{@code /swagger-ui.html}  — Swagger UI redirect</li>
 *   <li>{@code /ws/**}            — WebSocket upgrade handshake</li>
 * </ul>
 *
 * <p>Note: {@link UserDetailsServiceImpl} is a separate {@code @Service} bean (not
 * defined inline here) to avoid a circular dependency:
 * {@code SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter    jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsServiceImpl     userDetailsService;

    /**
     * Paths that never require a JWT token.
     * Keep this list minimal — prefer method-level {@code @PreAuthorize} over URL patterns.
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",     // Auth endpoints (login, register, refresh)
            "/actuator/health",    // Health probe — no sensitive data exposed
            "/v3/api-docs/**",     // OpenAPI JSON spec (Springdoc default)
            "/api-docs/**",        // OpenAPI JSON spec (custom path in application.yml)
            "/swagger-ui/**",      // Swagger UI static assets
            "/swagger-ui.html",    // Swagger UI entry redirect
            "/ws/**"               // WebSocket STOMP handshake
    };

    // ── Filter Chain ─────────────────────────────────────────────────────────

    /**
     * Main security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — stateless API, no server-side session</li>
     *   <li>Sessions stateless — JWT is the only auth mechanism</li>
     *   <li>JWT filter runs before {@link UsernamePasswordAuthenticationFilter}</li>
     *   <li>401 responses use {@link JwtAuthenticationEntryPoint} (JSON body)</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Authentication Beans ──────────────────────────────────────────────────

    /**
     * DAO-based authentication provider wiring together the
     * {@link UserDetailsServiceImpl} and {@link PasswordEncoder}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so it can be injected
     * into the future {@code AuthService} without circular dependencies.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder.
     * Cost factor 12 — strong enough for production without unacceptable latency.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

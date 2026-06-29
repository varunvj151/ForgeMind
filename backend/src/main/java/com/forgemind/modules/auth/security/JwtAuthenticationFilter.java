package com.forgemind.modules.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter — runs once per request.
 *
 * <p>Reads the {@code Authorization: Bearer <token>} header, validates the JWT via {@link
 * JwtService}, and sets the {@link org.springframework.security.core.Authentication} in the {@link
 * SecurityContextHolder} so that downstream Spring Security processing treats the request as
 * authenticated.
 *
 * <p>If no token is present, or the token is invalid/expired, the filter simply passes the request
 * along. The endpoint access decision is then made by Spring Security's authorization layer —
 * unauthenticated requests to protected resources are rejected by {@link
 * JwtAuthenticationEntryPoint}.
 *
 * <p>Registered in {@link com.forgemind.config.SecurityConfig} before {@link
 * org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTH_HEADER = "Authorization";

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader(AUTH_HEADER);

    // 1. Fast-path: no Authorization header or not a Bearer token → skip
    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = authHeader.substring(BEARER_PREFIX.length());
    final String username;

    // 2. Extract subject claim — any parse/validation error means skip
    try {
      username = jwtService.extractUsername(jwt);
    } catch (Exception ex) {
      log.debug(
          "JWT parsing failed for request [{}]: {}", request.getRequestURI(), ex.getMessage());
      filterChain.doFilter(request, response);
      return;
    }

    // 3. If username present and no authentication set yet, authenticate
    if (StringUtils.hasText(username)
        && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.debug("JWT authenticated user '{}' for [{}]", username, request.getRequestURI());
      }
    }

    filterChain.doFilter(request, response);
  }
}

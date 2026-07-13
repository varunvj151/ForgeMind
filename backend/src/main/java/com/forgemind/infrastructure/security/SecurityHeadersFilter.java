package com.forgemind.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds security headers to every HTTP response.
 * Implements OWASP security header recommendations.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // Prevent clickjacking
    response.setHeader("X-Frame-Options", "DENY");

    // Prevent MIME-type sniffing
    response.setHeader("X-Content-Type-Options", "nosniff");

    // Force HTTPS (HSTS) for 1 year
    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

    // Referrer policy
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

    // Permissions policy
    response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

    // Content Security Policy
    response.setHeader("Content-Security-Policy",
        "default-src 'self'; "
        + "script-src 'self'; "
        + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
        + "font-src 'self' https://fonts.gstatic.com; "
        + "img-src 'self' data: https:; "
        + "connect-src 'self' wss:; "
        + "frame-ancestors 'none';");

    filterChain.doFilter(request, response);
  }
}

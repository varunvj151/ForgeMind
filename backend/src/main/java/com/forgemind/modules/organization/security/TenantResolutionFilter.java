package com.forgemind.modules.organization.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that resolves the current tenant (organization) from the incoming request.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>{@code X-Organization-Id} header (UUID)
 *   <li>{@code orgId} query parameter (UUID)
 * </ol>
 *
 * <p>If no organization is resolved, the filter still proceeds — endpoints that require a tenant
 * will enforce this via {@link TenantIsolationInterceptor}.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class TenantResolutionFilter extends OncePerRequestFilter {

  static final String ORG_HEADER = "X-Organization-Id";

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String headerValue = request.getHeader(ORG_HEADER);
      if (headerValue != null && !headerValue.isBlank()) {
        try {
          UUID orgId = UUID.fromString(headerValue.trim());
          TenantContext.setOrganizationId(orgId);
          log.debug("Tenant resolved from header: {}", orgId);
        } catch (IllegalArgumentException e) {
          log.warn("Invalid X-Organization-Id header value: {}", headerValue);
        }
      }
      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }
}

package com.forgemind.modules.organization.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that enforces per-user rate limits on sensitive endpoints.
 *
 * <p>Rate-limited paths: {@code /api/v1/ai/**}, {@code /api/v1/git/**},
 * {@code /api/v1/auth/**} (auth endpoints use stricter limits).
 *
 * <p>Unauthenticated requests are limited per IP address.
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private static final Set<String> RATE_LIMITED_PREFIXES = Set.of(
      "/api/v1/ai/", "/api/v1/git/", "/api/v1/auth/");

  private static final int AI_CAPACITY = 30;         // 30 AI requests per minute
  private static final int GIT_CAPACITY = 60;        // 60 Git requests per minute
  private static final int AUTH_CAPACITY = 10;       // 10 auth requests per minute
  private static final int DEFAULT_CAPACITY = 120;   // default for other paths

  private final InMemoryRateLimiter rateLimiter;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (!isRateLimited(path)) {
      chain.doFilter(request, response);
      return;
    }

    String bucketKey = resolveBucketKey(request);
    int capacity = resolveCapacity(path);

    if (!rateLimiter.tryConsume(bucketKey, capacity, 1)) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
      response.setHeader("X-RateLimit-Remaining", "0");
      response.setHeader("Retry-After", "60");
      response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
      return;
    }

    response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
    response.setHeader("X-RateLimit-Remaining",
        String.valueOf(rateLimiter.getRemaining(bucketKey)));
    chain.doFilter(request, response);
  }

  private boolean isRateLimited(String path) {
    return RATE_LIMITED_PREFIXES.stream().anyMatch(path::startsWith);
  }

  private String resolveBucketKey(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
      return "user:" + auth.getName();
    }
    return "ip:" + request.getRemoteAddr();
  }

  private int resolveCapacity(String path) {
    if (path.startsWith("/api/v1/ai/")) return AI_CAPACITY;
    if (path.startsWith("/api/v1/auth/")) return AUTH_CAPACITY;
    if (path.startsWith("/api/v1/git/")) return GIT_CAPACITY;
    return DEFAULT_CAPACITY;
  }
}

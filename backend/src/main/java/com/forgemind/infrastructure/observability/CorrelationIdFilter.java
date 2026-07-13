package com.forgemind.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Injects correlation/request IDs into every request for distributed tracing.
 * IDs flow via: HTTP Header → MDC → Logs → Response Header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER  = "X-Request-Id";
  public static final String CORRELATION_HEADER = "X-Correlation-Id";
  public static final String MDC_REQUEST_ID     = "requestId";
  public static final String MDC_TRACE_ID       = "traceId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String requestId = generateOrExtract(request, REQUEST_ID_HEADER);
    String correlationId = generateOrExtract(request, CORRELATION_HEADER);

    try {
      MDC.put(MDC_REQUEST_ID, requestId);
      MDC.put(MDC_TRACE_ID, correlationId);

      response.setHeader(REQUEST_ID_HEADER, requestId);
      response.setHeader(CORRELATION_HEADER, correlationId);

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private String generateOrExtract(HttpServletRequest request, String header) {
    String value = request.getHeader(header);
    return (value != null && !value.isBlank()) ? value : UUID.randomUUID().toString();
  }
}

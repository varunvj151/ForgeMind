package com.forgemind.modules.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles unauthenticated access to protected resources.
 *
 * <p>Called by Spring Security when a request reaches a secured endpoint without
 * a valid (or any) JWT token. Returns a {@code 401 Unauthorized} response using
 * the standard {@link ErrorResponse} envelope so the frontend can handle it uniformly.
 *
 * <p>Without this, Spring Security would return a redirect to a login page (for
 * browser flows) or a plain {@code 401} with no body — neither is appropriate for
 * a stateless REST API consumed by an SPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest       request,
            HttpServletResponse      response,
            AuthenticationException  authException
    ) throws IOException {

        log.debug("Unauthenticated request to [{}]: {}", request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .code("UNAUTHORIZED")
                .message("Authentication required. Please provide a valid Bearer token.")
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}

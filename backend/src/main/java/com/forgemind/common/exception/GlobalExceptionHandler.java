package com.forgemind.common.exception;

import com.forgemind.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler providing a consistent error response structure
 * across all controllers, as specified in {@code 15-api-versioning.md §5}.
 *
 * <p>Handles:
 * <ul>
 *   <li>{@link ForgemindException} — application-level domain errors</li>
 *   <li>{@link MethodArgumentNotValidException} — @Valid request body failures</li>
 *   <li>{@link ConstraintViolationException} — @Validated path/query parameter failures</li>
 *   <li>{@link Exception} — unhandled exceptions (500 internal error)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Domain Exceptions ──────────────────────────────────────────────────────

    @ExceptionHandler(ForgemindException.class)
    public ResponseEntity<ErrorResponse> handleForgemindException(
            ForgemindException ex, HttpServletRequest request) {

        log.warn("Domain exception on {}: {} — {}", request.getRequestURI(), ex.getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorResponse.of(ex.getCode(), ex.getMessage(), ex.getHttpStatus().value(), request.getRequestURI()));
    }

    // ── Validation: Request Body ───────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .issue(fe.getDefaultMessage())
                        .build())
                .toList();

        log.debug("Validation failure on {}: {}", request.getRequestURI(), fieldErrors);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", 400,
                        request.getRequestURI(), fieldErrors));
    }

    // ── Validation: Path / Query Params ───────────────────────────────────────

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.debug("Constraint violation on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), 400, request.getRequestURI()));
    }

    // ── Catch-All (500) ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred", 500, request.getRequestURI()));
    }
}

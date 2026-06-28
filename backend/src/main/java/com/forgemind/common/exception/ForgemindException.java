package com.forgemind.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base application exception for all ForgeMind domain errors.
 *
 * <p>Every module-specific exception (e.g., {@code ProjectNotFoundException},
 * {@code InvalidCredentialsException}) should extend this class, providing
 * its own {@link HttpStatus} and error {@code code} string.
 *
 * <p>The {@link GlobalExceptionHandler} catches this exception type and maps it
 * to a consistent {@link com.forgemind.common.dto.ErrorResponse} structure.
 */
@Getter
public class ForgemindException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    public ForgemindException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public ForgemindException(String code, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}

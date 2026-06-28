package com.forgemind.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope returned by the {@link com.forgemind.common.exception.GlobalExceptionHandler}.
 *
 * <p>Conforms to the error format specified in {@code 15-api-versioning.md §5}.
 *
 * <pre>
 * {
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "email must be a valid email address",
 *     "status": 400,
 *     "path": "/api/v1/auth/register",
 *     "timestamp": "2026-06-27T10:00:00Z",
 *     "details": [...]
 *   }
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final ErrorBody error;

    public static ErrorResponse of(String code, String message, int status, String path) {
        return ErrorResponse.builder()
                .error(ErrorBody.builder()
                        .code(code)
                        .message(message)
                        .status(status)
                        .path(path)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static ErrorResponse of(String code, String message, int status, String path, List<FieldError> details) {
        return ErrorResponse.builder()
                .error(ErrorBody.builder()
                        .code(code)
                        .message(message)
                        .status(status)
                        .path(path)
                        .timestamp(Instant.now())
                        .details(details)
                        .build())
                .build();
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorBody {
        private final String code;
        private final String message;
        private final int status;
        private final String path;
        private final Instant timestamp;
        private final List<FieldError> details;
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String issue;
    }
}

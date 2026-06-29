package com.forgemind.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/** Standard error response envelope returned by the GlobalExceptionHandler. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private final Instant timestamp;
  private final int status;
  private final String code;
  private final String message;
  private final String path;

  // Validation errors (field -> message). Only included if non-null.
  private final Map<String, String> errors;
}

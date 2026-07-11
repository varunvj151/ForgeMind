package com.forgemind.modules.ai.tools;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when a tool cannot be found or fails while executing. Distinct from
 * {@link com.forgemind.modules.ai.exception.AiProviderException} so callers can tell "the domain
 * lookup failed" apart from "the LLM call failed".
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ToolExecutionException extends RuntimeException {

  public ToolExecutionException(String message) {
    super(message);
  }

  public ToolExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}

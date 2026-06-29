package com.forgemind.common.exception;

/** Exception thrown when a requested resource is not found. */
public class ResourceNotFoundException extends RuntimeException {

  private final String code;

  public ResourceNotFoundException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}

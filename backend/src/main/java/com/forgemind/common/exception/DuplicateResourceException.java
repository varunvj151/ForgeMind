package com.forgemind.common.exception;

/**
 * Exception thrown when a resource already exists and cannot be duplicated.
 */
public class DuplicateResourceException extends RuntimeException {

    private final String code;

    public DuplicateResourceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

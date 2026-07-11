package com.forgemind.modules.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class AiRateLimitException extends RuntimeException {

    public AiRateLimitException(String message) {
        super(message);
    }
}

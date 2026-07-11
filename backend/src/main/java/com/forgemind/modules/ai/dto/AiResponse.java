package com.forgemind.modules.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiResponse {

    /**
     * The generated text response from the LLM.
     */
    private String content;

    /**
     * The number of tokens used for the prompt.
     */
    private int promptTokens;

    /**
     * The number of tokens used for the generated response.
     */
    private int completionTokens;

    /**
     * The total number of tokens used.
     */
    private int totalTokens;

    /**
     * Provider used for this request.
     */
    private String provider;

    /**
     * Model used for this request.
     */
    private String model;
}

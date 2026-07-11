package com.forgemind.modules.ai.provider;

import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;

/**
 * Standard interface for interacting with any AI provider (e.g., Mock, Gemini, OpenAI).
 * Business logic should only depend on this interface, never a vendor-specific SDK.
 */
public interface AiProvider {

    /**
     * Generates a response based on the provided request parameters.
     *
     * @param request The AI request containing prompts and configuration.
     * @return The standardized AI response.
     */
    AiResponse generate(AiRequest request);
}

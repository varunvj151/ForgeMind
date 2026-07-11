package com.forgemind.modules.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /**
     * Provider type, e.g., "MOCK", "GEMINI", "OPENAI".
     */
    private String provider = "MOCK";

    /**
     * API key for the chosen provider.
     */
    private String apiKey;

    /**
     * Model name (e.g., gemini-1.5-pro-latest, gpt-4o).
     */
    private String model = "gemini-1.5-pro-latest";

    /**
     * Temperature setting for LLM responses.
     */
    private Double temperature = 0.7;

    /**
     * Maximum tokens to generate.
     */
    private Integer maxTokens = 2048;

    /**
     * Timeout for provider API calls in milliseconds.
     */
    private Integer timeoutMs = 30000;
}

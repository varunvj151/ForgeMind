package com.forgemind.modules.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRequest {

    /**
     * System-level instructions that define the AI's persona, rules, and output format.
     */
    private String systemPrompt;

    /**
     * The actual user query or structured prompt after context interpolation.
     */
    private String userPrompt;

    /**
     * Optional override for model name. If null, the default from AiProperties is used.
     */
    private String modelOverride;

    /**
     * Optional override for temperature.
     */
    private Double temperatureOverride;

    /**
     * Is JSON mode requested (if supported by provider)?
     */
    @Builder.Default
    private boolean jsonMode = false;
}

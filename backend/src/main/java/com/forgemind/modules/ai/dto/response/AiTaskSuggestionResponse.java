package com.forgemind.modules.ai.dto.response;

import lombok.Data;

@Data
public class AiTaskSuggestionResponse {
    private String title;
    private String description;
    private String priority;
}

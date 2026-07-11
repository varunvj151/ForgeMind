package com.forgemind.modules.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiGenerateTasksRequest {
    
    @NotBlank(message = "Feature description is required")
    private String featureDescription;
}

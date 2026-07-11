package com.forgemind.modules.ai.dto.response;

import lombok.Data;

@Data
public class AiRiskAssessmentResponse {
    private String riskLevel;
    private String summary;
    private String recommendation;
}

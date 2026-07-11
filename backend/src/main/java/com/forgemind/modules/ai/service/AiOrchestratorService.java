package com.forgemind.modules.ai.service;

import com.forgemind.modules.ai.dto.response.AiRiskAssessmentResponse;
import com.forgemind.modules.ai.dto.response.AiTaskSuggestionResponse;
import com.forgemind.modules.ai.dto.response.AiTextResponse;
import java.util.List;
import java.util.UUID;

public interface AiOrchestratorService {

    /**
     * Generates a list of suggested tasks based on project context and feature description.
     */
    List<AiTaskSuggestionResponse> generateTasks(UUID projectId, String featureDescription);

    /**
     * Generates an executive summary of the project.
     */
    AiTextResponse summarizeProject(UUID projectId);

    /**
     * Assesses technical and scheduling risks for the project.
     */
    List<AiRiskAssessmentResponse> assessRisks(UUID projectId);
    
    /**
     * Generates a README documentation for the project.
     */
    AiTextResponse generateReadme(UUID projectId);
}

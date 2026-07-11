package com.forgemind.modules.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.context.AiContextBuilder;
import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.dto.response.AiRiskAssessmentResponse;
import com.forgemind.modules.ai.dto.response.AiTaskSuggestionResponse;
import com.forgemind.modules.ai.dto.response.AiTextResponse;
import com.forgemind.modules.ai.exception.AiProviderException;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiOrchestratorServiceImpl implements AiOrchestratorService {

    private final AiProvider aiProvider;
    private final AiContextBuilder contextBuilder;
    private final PromptTemplateManager promptTemplateManager;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiTaskSuggestionResponse> generateTasks(UUID projectId, String featureDescription) {
        log.info("Generating tasks for project {} with feature: {}", projectId, featureDescription);
        
        Map<String, Object> context = contextBuilder.buildProjectContext(projectId);
        context.put("featureDescription", featureDescription);

        String userPrompt = promptTemplateManager.resolveTemplate(PromptTemplateManager.TASK_GENERATION_USER_PROMPT, context);
        
        AiRequest request = AiRequest.builder()
                .systemPrompt(PromptTemplateManager.TASK_GENERATION_SYSTEM_PROMPT)
                .userPrompt(userPrompt)
                .jsonMode(true)
                .build();

        AiResponse aiResponse = aiProvider.generate(request);
        return parseJsonArray(aiResponse.getContent(), new TypeReference<>() {});
    }

    @Override
    public AiTextResponse summarizeProject(UUID projectId) {
        log.info("Generating summary for project {}", projectId);
        
        Map<String, Object> context = contextBuilder.buildProjectContext(projectId);
        String userPrompt = promptTemplateManager.resolveTemplate(PromptTemplateManager.PROJECT_SUMMARY_USER_PROMPT, context);
        
        AiRequest request = AiRequest.builder()
                .systemPrompt(PromptTemplateManager.PROJECT_SUMMARY_SYSTEM_PROMPT)
                .userPrompt(userPrompt)
                .jsonMode(false)
                .build();

        AiResponse aiResponse = aiProvider.generate(request);
        return new AiTextResponse(aiResponse.getContent());
    }

    @Override
    public List<AiRiskAssessmentResponse> assessRisks(UUID projectId) {
        log.info("Assessing risks for project {}", projectId);
        
        Map<String, Object> context = contextBuilder.buildProjectContext(projectId);
        String userPrompt = promptTemplateManager.resolveTemplate(PromptTemplateManager.RISK_ASSESSMENT_USER_PROMPT, context);
        
        AiRequest request = AiRequest.builder()
                .systemPrompt(PromptTemplateManager.RISK_ASSESSMENT_SYSTEM_PROMPT)
                .userPrompt(userPrompt)
                .jsonMode(true)
                .build();

        AiResponse aiResponse = aiProvider.generate(request);
        return parseJsonArray(aiResponse.getContent(), new TypeReference<>() {});
    }

    @Override
    public AiTextResponse generateReadme(UUID projectId) {
        log.info("Generating README for project {}", projectId);
        
        Map<String, Object> context = contextBuilder.buildProjectContext(projectId);
        
        String systemPrompt = "You are a senior developer writing a README.md file. Use markdown format. Generate a comprehensive README based on the project data.";
        String userPrompt = "Project Context (JSON):\n" + context.get("contextJson") + "\n\nPlease write the README.";
        
        AiRequest request = AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .jsonMode(false)
                .build();

        AiResponse aiResponse = aiProvider.generate(request);
        return new AiTextResponse(aiResponse.getContent());
    }

    /**
     * Safely parse a JSON array string returned by the LLM (handling markdown blocks if present).
     */
    private <T> List<T> parseJsonArray(String jsonContent, TypeReference<List<T>> typeReference) {
        try {
            // Strip markdown block formatting if the LLM wrapped the JSON
            String cleanJson = jsonContent.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleanJson, typeReference);
        } catch (Exception e) {
            log.error("Failed to parse AI JSON response: {}", jsonContent, e);
            throw new AiProviderException("AI returned invalid JSON format", e);
        }
    }
}

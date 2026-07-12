package com.forgemind.modules.git.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.AbstractAgent;
import com.forgemind.modules.ai.agent.AgentCapability;
import com.forgemind.modules.ai.agent.AgentPrompts;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.git.ai.dto.CodeReviewResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CodeReviewAgent extends AbstractAgent<CodeReviewResult> {

  private final ObjectMapper objectMapper;

  public CodeReviewAgent(
      AiProvider aiProvider,
      PromptTemplateManager templateManager,
      com.forgemind.modules.ai.tools.ToolExecutor toolExecutor,
      ObjectMapper objectMapper,
      com.forgemind.modules.ai.agent.security.AgentAccessGuard accessGuard,
      AgentMetrics metrics) {
    super(aiProvider, templateManager, toolExecutor, objectMapper, accessGuard, metrics);
    this.objectMapper = objectMapper;
  }

  @Override
  public AgentCapability capability() {
    return AgentCapability.CODE_REVIEW;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.stringParam("codeChunks").isEmpty()) {
      throw new IllegalArgumentException("codeChunks is required for code review");
    }
  }

  @Override
  protected ExecutionResult<CodeReviewResult> doExecute(AgentRequest request, com.forgemind.modules.ai.memory.AgentMemory executionMetadata) {
      
    String systemPrompt = AgentPrompts.CODE_REVIEW_SYSTEM;
    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.CODE_REVIEW_USER, Map.of(
        "repositoryName", request.stringParam("repositoryName").orElse("Unknown"),
        "languages", request.stringParam("languages").orElse("Unknown"),
        "codeChunks", request.stringParam("codeChunks").get(),
        "projectContext", request.stringParam("projectContext").orElse("{}")
    ));

    com.forgemind.modules.ai.dto.AiRequest aiReq = com.forgemind.modules.ai.dto.AiRequest.builder()
        .systemPrompt(systemPrompt)
        .userPrompt(userPrompt)
        .jsonMode(true)
        .build();
    com.forgemind.modules.ai.dto.AiResponse response = aiProvider.generate(aiReq);
    String jsonResponse = response.getContent();
    
    // Remove markdown code fences if model hallucinated them
    jsonResponse = jsonResponse.replaceAll("(?s)^```json\\n(.*)\\n```$", "$1").trim();

    try {
      CodeReviewResult payload = objectMapper.readValue(jsonResponse, CodeReviewResult.class);
      return new ExecutionResult<>(payload, response);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to parse Code Review AI response: " + jsonResponse, e);
    }
  }
}

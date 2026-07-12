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
import com.forgemind.modules.git.ai.dto.ReleaseNotesResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReleaseNotesAgent extends AbstractAgent<ReleaseNotesResult> {

  private final ObjectMapper objectMapper;

  public ReleaseNotesAgent(
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
    return AgentCapability.RELEASE_NOTES;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.stringParam("commitsJson").isEmpty()) {
      throw new IllegalArgumentException("commitsJson is required for release notes");
    }
  }

  @Override
  protected ExecutionResult<ReleaseNotesResult> doExecute(AgentRequest request, com.forgemind.modules.ai.memory.AgentMemory executionMetadata) {
      
    String systemPrompt = AgentPrompts.RELEASE_NOTES_SYSTEM;
    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.RELEASE_NOTES_USER, Map.of(
        "version", request.stringParam("version").orElse("v1.0.0"),
        "repositoryName", request.stringParam("repositoryName").orElse("Unknown"),
        "dateFrom", request.stringParam("dateFrom").orElse("Unknown"),
        "dateTo", request.stringParam("dateTo").orElse("Unknown"),
        "commitsJson", request.stringParam("commitsJson").get(),
        "pullRequestsJson", request.stringParam("pullRequestsJson").orElse("[]"),
        "tasksJson", request.stringParam("tasksJson").orElse("[]")
    ));

    com.forgemind.modules.ai.dto.AiRequest aiReq = com.forgemind.modules.ai.dto.AiRequest.builder()
        .systemPrompt(systemPrompt)
        .userPrompt(userPrompt)
        .jsonMode(true)
        .build();
    com.forgemind.modules.ai.dto.AiResponse response = aiProvider.generate(aiReq);
    String jsonResponse = response.getContent();
    jsonResponse = jsonResponse.replaceAll("(?s)^```json\\n(.*)\\n```$", "$1").trim();

    try {
      ReleaseNotesResult payload = objectMapper.readValue(jsonResponse, ReleaseNotesResult.class);
      return new ExecutionResult<>(payload, response);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to parse Release Notes AI response: " + jsonResponse, e);
    }
  }
}

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
import com.forgemind.modules.git.ai.dto.ArchitectureResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ArchitectureAgent extends AbstractAgent<ArchitectureResult> {

  private final ObjectMapper objectMapper;

  public ArchitectureAgent(
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
    return AgentCapability.ARCHITECTURE_ANALYSIS;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.stringParam("fileStructure").isEmpty()) {
      throw new IllegalArgumentException("fileStructure is required for architecture analysis");
    }
  }

  @Override
  protected ExecutionResult<ArchitectureResult> doExecute(AgentRequest request, com.forgemind.modules.ai.memory.AgentMemory executionMetadata) {
      
    String systemPrompt = AgentPrompts.ARCHITECTURE_SYSTEM;
    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.ARCHITECTURE_USER, Map.of(
        "repositoryName", request.stringParam("repositoryName").orElse("Unknown"),
        "primaryLanguage", request.stringParam("primaryLanguage").orElse("Unknown"),
        "fileStructure", request.stringParam("fileStructure").get(),
        "codeChunks", request.stringParam("codeChunks").orElse("[]")
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
      ArchitectureResult payload = objectMapper.readValue(jsonResponse, ArchitectureResult.class);
      return new ExecutionResult<>(payload, response);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to parse Architecture Analysis AI response: " + jsonResponse, e);
    }
  }
}

package com.forgemind.modules.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.DocumentationResult;
import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.context.AiContextBuilder;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.memory.AgentMemory;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.tools.ToolExecutor;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Generates project documentation of a requested type — README, ARCHITECTURE, API, RELEASE_NOTES,
 * SPRINT_REPORT or SUMMARY.
 *
 * <p>Gathers context automatically (reusing {@link AiContextBuilder}) before prompting the provider,
 * so the caller only has to name the document type. Documentation is prose, so the payload carries
 * the generated markdown rather than a decomposed structure.
 */
@Component
public class DocumentationAgent extends AbstractAgent<DocumentationResult> {

  /** Request parameter key naming which document to produce. */
  public static final String PARAM_DOC_TYPE = "documentType";

  private static final String DEFAULT_DOC_TYPE = "README";

  /** Document types this agent knows how to produce. */
  private static final Set<String> SUPPORTED_TYPES =
      Set.of("README", "ARCHITECTURE", "API", "RELEASE_NOTES", "SPRINT_REPORT", "SUMMARY");

  private final AiContextBuilder contextBuilder;

  public DocumentationAgent(
      AiProvider aiProvider,
      PromptTemplateManager promptTemplateManager,
      ToolExecutor toolExecutor,
      ObjectMapper objectMapper,
      AgentAccessGuard accessGuard,
      AgentMetrics agentMetrics,
      AiContextBuilder contextBuilder) {
    super(aiProvider, promptTemplateManager, toolExecutor, objectMapper, accessGuard, agentMetrics);
    this.contextBuilder = contextBuilder;
  }

  @Override
  public AgentCapability capability() {
    return AgentCapability.DOCUMENTATION;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.getProjectId() == null) {
      throw new IllegalArgumentException("DocumentationAgent requires a projectId.");
    }
    String type = request.stringParam(PARAM_DOC_TYPE).orElse(DEFAULT_DOC_TYPE).toUpperCase();
    if (!SUPPORTED_TYPES.contains(type)) {
      throw new IllegalArgumentException(
          "Unsupported documentType '" + type + "'. Supported: " + SUPPORTED_TYPES);
    }
  }

  @Override
  protected ExecutionResult<DocumentationResult> doExecute(AgentRequest request, AgentMemory memory) {
    String docType =
        request.stringParam(PARAM_DOC_TYPE).orElse(DEFAULT_DOC_TYPE).toUpperCase();

    Map<String, Object> context = contextBuilder.buildProjectContext(request.getProjectId());
    context.put(PARAM_DOC_TYPE, docType);
    memory.put("context", context);

    String systemPrompt =
        promptTemplateManager.resolveTemplate(AgentPrompts.DOC_SYSTEM, context);
    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.DOC_USER, context);

    // Documentation is free-form markdown → text mode, not JSON.
    AiResponse aiResponse = callProvider(systemPrompt, userPrompt, false);

    DocumentationResult result =
        new DocumentationResult(docType, docType + " Documentation", aiResponse.getContent());
    return new ExecutionResult<>(result, aiResponse);
  }
}

package com.forgemind.modules.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.PlanResult;
import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.context.AiContextBuilder;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.memory.AgentMemory;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.rag.RagContext;
import com.forgemind.modules.ai.rag.RagOrchestrator;
import com.forgemind.modules.ai.tools.ToolExecutor;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Turns a free-form feature description into a structured delivery plan: a feature summary,
 * milestones (with priority and effort) and phased, dependency-aware task suggestions.
 *
 * <p>Reuses {@link AiContextBuilder} to fold the existing project + task state into the prompt so
 * the plan is grounded in what already exists rather than produced in a vacuum.
 */
@Component
public class PlannerAgent extends AbstractAgent<PlanResult> {

  /** Request parameter key carrying the feature to plan. */
  public static final String PARAM_FEATURE = "featureDescription";

  private final AiContextBuilder contextBuilder;
  private final RagOrchestrator ragOrchestrator;

  public PlannerAgent(
      AiProvider aiProvider,
      PromptTemplateManager promptTemplateManager,
      ToolExecutor toolExecutor,
      ObjectMapper objectMapper,
      AgentAccessGuard accessGuard,
      AgentMetrics agentMetrics,
      AiContextBuilder contextBuilder,
      RagOrchestrator ragOrchestrator) {
    super(aiProvider, promptTemplateManager, toolExecutor, objectMapper, accessGuard, agentMetrics);
    this.contextBuilder = contextBuilder;
    this.ragOrchestrator = ragOrchestrator;
  }

  @Override
  public AgentCapability capability() {
    return AgentCapability.PLANNING;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.getProjectId() == null) {
      throw new IllegalArgumentException("PlannerAgent requires a projectId.");
    }
    request
        .stringParam(PARAM_FEATURE)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "PlannerAgent requires a non-blank '" + PARAM_FEATURE + "' parameter."));
  }

  @Override
  protected ExecutionResult<PlanResult> doExecute(AgentRequest request, AgentMemory memory) {
    String feature = request.stringParam(PARAM_FEATURE).orElseThrow();

    // Gather grounding context (reuses the existing builder).
    Map<String, Object> context = contextBuilder.buildProjectContext(request.getProjectId());
    context.put("featureDescription", feature);
    memory.put("context", context);

    String userPrompt =
        promptTemplateManager.resolveTemplate(AgentPrompts.PLANNER_USER, Map.of("description", feature));

    RagContext ragContext = ragOrchestrator.augmentPrompt(request.getProjectId(), userPrompt);

    AiResponse aiResponse = callProvider(AgentPrompts.PLANNER_SYSTEM, ragContext.augmentedPrompt(), true);
    memory.recordToolResult("provider", aiResponse.getContent());

    PlanResult plan = parseJsonObject(aiResponse.getContent(), PlanResult.class);
    return new ExecutionResult<>(plan, aiResponse);
  }
}

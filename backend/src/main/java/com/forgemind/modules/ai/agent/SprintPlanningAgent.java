package com.forgemind.modules.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.SprintPlanResult;
import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.exception.AiProviderException;
import com.forgemind.modules.ai.memory.AgentMemory;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.rag.RagContext;
import com.forgemind.modules.ai.rag.RagOrchestrator;
import com.forgemind.modules.ai.tools.ToolExecutor;
import com.forgemind.modules.ai.tools.ToolNames;
import com.forgemind.modules.ai.tools.impl.MetricsTool;
import com.forgemind.modules.ai.tools.impl.TaskTool;
import com.forgemind.modules.task.dto.response.TaskResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builds a sprint plan from the current backlog, a target sprint duration and team size.
 *
 * <p>Composes the task and metrics tools to ground the plan, then asks the provider to select,
 * order and assign backlog items within realistic capacity, returning a structured
 * {@link SprintPlanResult}.
 */
@Component
public class SprintPlanningAgent extends AbstractAgent<SprintPlanResult> {

  private final RagOrchestrator ragOrchestrator;

  public static final String PARAM_DURATION_DAYS = "sprintDurationDays";
  public static final String PARAM_TEAM_SIZE = "teamSize";

  private static final int DEFAULT_DURATION_DAYS = 14;
  private static final int DEFAULT_TEAM_SIZE = 3;

  public SprintPlanningAgent(
      AiProvider aiProvider,
      PromptTemplateManager promptTemplateManager,
      ToolExecutor toolExecutor,
      ObjectMapper objectMapper,
      AgentAccessGuard accessGuard,
      AgentMetrics agentMetrics,
      RagOrchestrator ragOrchestrator) {
    super(aiProvider, promptTemplateManager, toolExecutor, objectMapper, accessGuard, agentMetrics);
    this.ragOrchestrator = ragOrchestrator;
  }

  @Override
  public AgentCapability capability() {
    return AgentCapability.SPRINT_PLANNING;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.getProjectId() == null) {
      throw new IllegalArgumentException("SprintPlanningAgent requires a projectId.");
    }
    int duration = request.intParam(PARAM_DURATION_DAYS).orElse(DEFAULT_DURATION_DAYS);
    int teamSize = request.intParam(PARAM_TEAM_SIZE).orElse(DEFAULT_TEAM_SIZE);
    if (duration <= 0) {
      throw new IllegalArgumentException(PARAM_DURATION_DAYS + " must be positive.");
    }
    if (teamSize <= 0) {
      throw new IllegalArgumentException(PARAM_TEAM_SIZE + " must be positive.");
    }
  }

  @Override
  protected ExecutionResult<SprintPlanResult> doExecute(AgentRequest request, AgentMemory memory) {
    int duration = request.intParam(PARAM_DURATION_DAYS).orElse(DEFAULT_DURATION_DAYS);
    int teamSize = request.intParam(PARAM_TEAM_SIZE).orElse(DEFAULT_TEAM_SIZE);

    List<TaskResponse> tasks =
        toolExecutor.execute(ToolNames.TASK, new TaskTool.Input(request.getProjectId()));
    MetricsTool.ProjectMetrics metrics = toolExecutor.execute(ToolNames.METRICS, tasks);

    memory.recordToolResult(ToolNames.TASK, tasks);
    memory.recordToolResult(ToolNames.METRICS, metrics);

    Map<String, Object> context = new HashMap<>();
    context.put(PARAM_DURATION_DAYS, String.valueOf(duration));
    context.put(PARAM_TEAM_SIZE, String.valueOf(teamSize));
    context.put("metricsJson", toJson(metrics));
    context.put("tasksJson", toJson(tasks));

    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.SPRINT_USER, context);
    
    RagContext ragContext = ragOrchestrator.augmentPrompt(request.getProjectId(), userPrompt);

    AiResponse aiResponse = callProvider(AgentPrompts.SPRINT_SYSTEM, ragContext.augmentedPrompt(), true);

    SprintPlanResult result = parseJsonObject(aiResponse.getContent(), SprintPlanResult.class);
    return new ExecutionResult<>(result, aiResponse);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new AiProviderException("Failed to serialize sprint-planning context", e);
    }
  }
}

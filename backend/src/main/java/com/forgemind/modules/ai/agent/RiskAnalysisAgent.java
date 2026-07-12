package com.forgemind.modules.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.RiskAnalysisResult;
import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.context.AiContextBuilder;
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
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.task.dto.response.TaskResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Assesses delivery risk for a project.
 *
 * <p>Composes tools rather than querying repositories: it pulls tasks ({@link TaskTool}), computes
 * deterministic metrics ({@link MetricsTool}), fetches recent activity and project metadata, then
 * asks the provider to reason over that ground truth. Feeding computed metrics (not raw task dumps
 * alone) keeps the LLM's risk score anchored to real numbers.
 */
@Component
public class RiskAnalysisAgent extends AbstractAgent<RiskAnalysisResult> {

  private final AiContextBuilder contextBuilder;
  private final RagOrchestrator ragOrchestrator;

  public RiskAnalysisAgent(
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
    return AgentCapability.RISK_ANALYSIS;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.getProjectId() == null) {
      throw new IllegalArgumentException("RiskAnalysisAgent requires a projectId.");
    }
  }

  @Override
  protected ExecutionResult<RiskAnalysisResult> doExecute(AgentRequest request, AgentMemory memory) {
    var projectId = request.getProjectId();

    // Compose tools through the executor — never touch repositories directly.
    List<TaskResponse> tasks =
        toolExecutor.execute(ToolNames.TASK, new TaskTool.Input(projectId));
    MetricsTool.ProjectMetrics metrics = toolExecutor.execute(ToolNames.METRICS, tasks);
    List<ActivityResponse> activity =
        toolExecutor.execute(
            ToolNames.ACTIVITY, new com.forgemind.modules.ai.tools.impl.ActivityTool.Input(projectId));
    ProjectResponse project = toolExecutor.execute(ToolNames.PROJECT, projectId);

    memory.recordToolResult(ToolNames.TASK, tasks);
    memory.recordToolResult(ToolNames.METRICS, metrics);
    memory.recordToolResult(ToolNames.ACTIVITY, activity);

    Map<String, Object> context = new HashMap<>();
    context.put("metricsJson", toJson(metrics));
    context.put("activityJson", toJson(activity));
    context.put("projectJson", toJson(project));

    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.RISK_USER, context);
    
    RagContext ragContext = ragOrchestrator.augmentPrompt(request.getProjectId(), userPrompt);

    AiResponse aiResponse = callProvider(AgentPrompts.RISK_SYSTEM, ragContext.augmentedPrompt(), true);

    RiskAnalysisResult result = parseJsonObject(aiResponse.getContent(), RiskAnalysisResult.class);
    return new ExecutionResult<>(result, aiResponse);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new AiProviderException("Failed to serialize risk-analysis context", e);
    }
  }
}

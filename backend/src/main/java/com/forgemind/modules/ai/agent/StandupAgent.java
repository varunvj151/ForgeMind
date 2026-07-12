package com.forgemind.modules.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.StandupResult;
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
import com.forgemind.modules.ai.tools.impl.ActivityTool;
import com.forgemind.modules.ai.tools.impl.TaskTool;
import com.forgemind.modules.task.dto.response.TaskResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Generates a daily stand-up (yesterday / today / blockers / upcoming priorities) for a project by
 * composing the activity and task tools and letting the provider narrate the recent momentum.
 */
@Component
public class StandupAgent extends AbstractAgent<StandupResult> {

  private final AiContextBuilder contextBuilder;
  private final RagOrchestrator ragOrchestrator;

  public StandupAgent(
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
    return AgentCapability.STANDUP;
  }

  @Override
  protected void validate(AgentRequest request) {
    if (request.getProjectId() == null) {
      throw new IllegalArgumentException("StandupAgent requires a projectId.");
    }
  }

  @Override
  protected ExecutionResult<StandupResult> doExecute(AgentRequest request, AgentMemory memory) {
    var projectId = request.getProjectId();

    List<ActivityResponse> activity =
        toolExecutor.execute(ToolNames.ACTIVITY, new ActivityTool.Input(projectId));
    List<TaskResponse> tasks = toolExecutor.execute(ToolNames.TASK, new TaskTool.Input(projectId));

    memory.recordToolResult(ToolNames.ACTIVITY, activity);
    memory.recordToolResult(ToolNames.TASK, tasks);

    Map<String, Object> context = new HashMap<>();
    context.put("activityJson", toJson(activity));
    context.put("tasksJson", toJson(tasks));

    String userPrompt = promptTemplateManager.resolveTemplate(AgentPrompts.STANDUP_USER, context);
    
    RagContext ragContext = ragOrchestrator.augmentPrompt(request.getProjectId(), userPrompt);

    AiResponse aiResponse = callProvider(AgentPrompts.STANDUP_SYSTEM, ragContext.augmentedPrompt(), true);

    StandupResult result = parseJsonObject(aiResponse.getContent(), StandupResult.class);
    return new ExecutionResult<>(result, aiResponse);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new AiProviderException("Failed to serialize stand-up context", e);
    }
  }
}

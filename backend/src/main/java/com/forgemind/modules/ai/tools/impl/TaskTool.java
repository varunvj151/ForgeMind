package com.forgemind.modules.ai.tools.impl;

import com.forgemind.modules.ai.tools.AITool;
import com.forgemind.modules.ai.tools.ToolNames;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.service.TaskService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Lists the tasks of a project (capped to keep prompt token usage bounded). Wraps {@link
 * TaskService}; agents compose this instead of querying task repositories.
 */
@Component
@RequiredArgsConstructor
public class TaskTool implements AITool<TaskTool.Input, List<TaskResponse>> {

  /** Default cap on tasks returned, matching the context builder's convention. */
  private static final int DEFAULT_LIMIT = 100;

  private final TaskService taskService;

  /** Input for the task tool: which project and how many tasks to pull. */
  public record Input(UUID projectId, Integer limit) {
    public Input(UUID projectId) {
      this(projectId, DEFAULT_LIMIT);
    }
  }

  @Override
  public String name() {
    return ToolNames.TASK;
  }

  @Override
  public String description() {
    return "Lists a project's tasks (id, title, status, priority, assignee, due date).";
  }

  @Override
  public List<TaskResponse> execute(Input input) {
    int limit = input.limit() != null && input.limit() > 0 ? input.limit() : DEFAULT_LIMIT;
    return taskService.listProjectTasks(input.projectId(), Pageable.ofSize(limit)).getContent();
  }
}

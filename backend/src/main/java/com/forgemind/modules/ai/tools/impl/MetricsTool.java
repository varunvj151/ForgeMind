package com.forgemind.modules.ai.tools.impl;

import com.forgemind.modules.ai.tools.AITool;
import com.forgemind.modules.ai.tools.ToolNames;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.entity.TaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Computes deterministic project health metrics from a task list — <em>without</em> calling an LLM.
 *
 * <p>Deriving hard numbers (overdue count, blocked count, completion ratio, status distribution) in
 * code and feeding them to the agent gives the LLM reliable ground truth to reason over, instead of
 * asking it to count for itself. Used by the risk and sprint agents.
 *
 * <p>Operates on already-fetched, access-checked {@link TaskResponse} DTOs, so it performs no data
 * access of its own.
 */
@Component
@RequiredArgsConstructor
public class MetricsTool implements AITool<List<TaskResponse>, MetricsTool.ProjectMetrics> {

  /** Structured, LLM-friendly snapshot of a project's task health. */
  public record ProjectMetrics(
      int totalTasks,
      int completedTasks,
      int inProgressTasks,
      int blockedTasks,
      int overdueTasks,
      double completionRatio,
      Map<String, Long> statusDistribution,
      Map<String, Long> priorityDistribution) {}

  @Override
  public String name() {
    return ToolNames.METRICS;
  }

  @Override
  public String description() {
    return "Computes deterministic health metrics (overdue, blocked, completion ratio, distributions) from a task list.";
  }

  @Override
  public ProjectMetrics execute(List<TaskResponse> tasks) {
    if (tasks == null || tasks.isEmpty()) {
      return new ProjectMetrics(0, 0, 0, 0, 0, 0.0, Map.of(), Map.of());
    }

    Instant now = Instant.now();
    int total = tasks.size();
    int completed = 0;
    int inProgress = 0;
    int blocked = 0;
    int overdue = 0;

    for (TaskResponse task : tasks) {
      if (task.status() == TaskStatus.DONE) {
        completed++;
      } else if (task.status() == TaskStatus.IN_PROGRESS || task.status() == TaskStatus.IN_REVIEW) {
        inProgress++;
      } else if (task.status() == TaskStatus.BLOCKED) {
        blocked++;
      }
      // Overdue = past due date and not yet done.
      if (task.dueDate() != null
          && task.dueDate().isBefore(now)
          && task.status() != TaskStatus.DONE) {
        overdue++;
      }
    }

    Map<String, Long> statusDistribution =
        tasks.stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    t -> t.status() == null ? "UNKNOWN" : t.status().name(),
                    java.util.stream.Collectors.counting()));

    Map<String, Long> priorityDistribution =
        tasks.stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    t -> t.priority() == null ? "UNKNOWN" : t.priority().name(),
                    java.util.stream.Collectors.counting()));

    double completionRatio = (double) completed / total;

    return new ProjectMetrics(
        total,
        completed,
        inProgress,
        blocked,
        overdue,
        Math.round(completionRatio * 1000.0) / 1000.0,
        statusDistribution,
        priorityDistribution);
  }
}

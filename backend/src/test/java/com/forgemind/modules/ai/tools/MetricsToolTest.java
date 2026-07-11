package com.forgemind.modules.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.forgemind.modules.ai.tools.impl.MetricsTool;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.entity.TaskPriority;
import com.forgemind.modules.task.entity.TaskStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for the deterministic {@link MetricsTool} computations. */
class MetricsToolTest {

  private final MetricsTool metricsTool = new MetricsTool();

  private TaskResponse task(TaskStatus status, TaskPriority priority, Instant dueDate) {
    return new TaskResponse(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Project",
        "Title",
        "Desc",
        status,
        priority,
        null,
        null,
        dueDate,
        Instant.now(),
        Instant.now());
  }

  @Test
  void execute_ShouldReturnZeroesForEmptyList() {
    MetricsTool.ProjectMetrics metrics = metricsTool.execute(List.of());

    assertEquals(0, metrics.totalTasks());
    assertEquals(0.0, metrics.completionRatio());
  }

  @Test
  void execute_ShouldCountStatusesAndOverdue() {
    Instant past = Instant.now().minus(2, ChronoUnit.DAYS);
    Instant future = Instant.now().plus(2, ChronoUnit.DAYS);

    List<TaskResponse> tasks =
        List.of(
            task(TaskStatus.DONE, TaskPriority.LOW, past), // done + past → NOT overdue
            task(TaskStatus.TODO, TaskPriority.HIGH, past), // overdue
            task(TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, future),
            task(TaskStatus.BLOCKED, TaskPriority.CRITICAL, null));

    MetricsTool.ProjectMetrics metrics = metricsTool.execute(tasks);

    assertEquals(4, metrics.totalTasks());
    assertEquals(1, metrics.completedTasks());
    assertEquals(1, metrics.inProgressTasks());
    assertEquals(1, metrics.blockedTasks());
    assertEquals(1, metrics.overdueTasks());
    assertEquals(0.25, metrics.completionRatio());
    assertEquals(1L, metrics.statusDistribution().get("BLOCKED"));
    assertEquals(1L, metrics.priorityDistribution().get("CRITICAL"));
  }
}

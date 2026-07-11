package com.forgemind.modules.ai.agent.dto;

import java.util.List;

/**
 * Structured output of the {@link com.forgemind.modules.ai.agent.SprintPlanningAgent}.
 *
 * <p>An ordered sprint backlog with dependencies, suggested assignments and a completion estimate.
 */
public record SprintPlanResult(
    int sprintDurationDays,
    int teamSize,
    String estimatedCompletion,
    List<SprintItem> backlog,
    List<String> notes) {

  /** One ordered backlog item selected for the sprint. */
  public record SprintItem(
      int order,
      String title,
      String priority,
      String estimatedEffort,
      String suggestedAssignee,
      List<String> dependsOn) {}
}

package com.forgemind.modules.ai.agent.dto;

import java.util.List;

/**
 * Structured output of the {@link com.forgemind.modules.ai.agent.PlannerAgent}.
 *
 * <p>Returned instead of raw text so the frontend and downstream automation can consume milestones,
 * phased tasks, dependencies and estimates directly.
 */
public record PlanResult(
    String featureSummary, List<Milestone> milestones, List<SuggestedTask> tasks) {

  /** A high-level milestone grouping a set of work. */
  public record Milestone(String name, String objective, String priority, String estimatedEffort) {}

  /** A concrete, actionable task suggestion with ordering hints. */
  public record SuggestedTask(
      String title,
      String description,
      String priority,
      String phase,
      String estimatedEffort,
      List<String> dependsOn) {}
}

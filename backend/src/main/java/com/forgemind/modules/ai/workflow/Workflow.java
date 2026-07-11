package com.forgemind.modules.ai.workflow;

import java.util.List;

/**
 * An ordered, named sequence of {@link WorkflowStep}s executed by the {@link WorkflowEngine}.
 *
 * <p>A workflow is pure data — it describes <em>what</em> to run, not <em>how</em>. This keeps the
 * engine generic and lets new multi-agent flows be declared without new orchestration code, matching
 * the platform's "add an agent, register it, reuse the framework" goal.
 */
public record Workflow(String name, List<WorkflowStep> steps) {

  public Workflow {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Workflow name is required.");
    }
    if (steps == null || steps.isEmpty()) {
      throw new IllegalArgumentException("Workflow must contain at least one step.");
    }
    steps = List.copyOf(steps);
  }

  /** Fluent builder for readability when declaring workflows in code. */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  public static final class Builder {
    private final String name;
    private final java.util.ArrayList<WorkflowStep> steps = new java.util.ArrayList<>();

    private Builder(String name) {
      this.name = name;
    }

    public Builder step(WorkflowStep step) {
      steps.add(step);
      return this;
    }

    public Workflow build() {
      return new Workflow(name, steps);
    }
  }
}

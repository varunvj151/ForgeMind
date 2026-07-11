package com.forgemind.modules.ai.workflow;

import com.forgemind.modules.ai.agent.AgentCapability;
import com.forgemind.modules.ai.agent.dto.AgentRequest;

/**
 * A single node in a {@link Workflow}: run the agent for {@code capability} with the request built
 * by {@code requestFactory}.
 *
 * <p>The factory receives the live {@link WorkflowContext}, so a step can be parameterised by the
 * results of earlier steps — this is what lets workflows chain agents (e.g. plan → then document the
 * plan). Kept intentionally minimal; richer control flow (branching, loops) can be layered on later
 * without changing the {@link WorkflowEngine} contract.
 */
public record WorkflowStep(
    String name, AgentCapability capability, RequestFactory requestFactory) {

  /** Builds the {@link AgentRequest} for a step from the accumulated workflow context. */
  @FunctionalInterface
  public interface RequestFactory {
    AgentRequest build(WorkflowContext context);
  }

  /** Convenience for a step whose request does not depend on prior results. */
  public static WorkflowStep of(
      String name, AgentCapability capability, AgentRequest fixedRequest) {
    return new WorkflowStep(name, capability, ctx -> fixedRequest);
  }
}

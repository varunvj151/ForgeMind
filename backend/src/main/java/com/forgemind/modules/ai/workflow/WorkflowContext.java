package com.forgemind.modules.ai.workflow;

import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Accumulates the results of each step as a {@link Workflow} runs, and exposes them to subsequent
 * steps' request factories.
 *
 * <p>Think of it as the workflow's short-term memory (the multi-step analogue of {@link
 * com.forgemind.modules.ai.memory.AgentMemory}): step N can read what steps 1..N-1 produced and
 * shape its own input accordingly. Not thread-safe — a workflow executes its steps sequentially.
 */
public class WorkflowContext {

  private final Map<String, AgentResponse<?>> stepResults = new LinkedHashMap<>();
  private final Map<String, Object> attributes = new LinkedHashMap<>();

  /** Records a completed step's response under the step name. */
  void recordStepResult(String stepName, AgentResponse<?> response) {
    stepResults.put(stepName, response);
  }

  /** Reads a prior step's response. */
  @SuppressWarnings("unchecked")
  public <T> Optional<AgentResponse<T>> resultOf(String stepName) {
    return Optional.ofNullable((AgentResponse<T>) stepResults.get(stepName));
  }

  /** Reads just the payload of a prior step's response. */
  @SuppressWarnings("unchecked")
  public <T> Optional<T> payloadOf(String stepName) {
    AgentResponse<?> response = stepResults.get(stepName);
    return response == null ? Optional.empty() : Optional.ofNullable((T) response.getPayload());
  }

  /** Arbitrary shared attribute set by the caller before the run (e.g. a projectId). */
  public WorkflowContext withAttribute(String key, Object value) {
    attributes.put(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> attribute(String key) {
    return Optional.ofNullable((T) attributes.get(key));
  }

  /** Ordered view of all step results (for building the final workflow result). */
  public Map<String, AgentResponse<?>> allResults() {
    return Map.copyOf(stepResults);
  }
}

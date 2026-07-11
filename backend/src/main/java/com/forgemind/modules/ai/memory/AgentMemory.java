package com.forgemind.modules.ai.memory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Short-term, single-execution scratchpad for an agent run.
 *
 * <p>Scope is deliberately narrow: one {@code AgentMemory} exists for the lifetime of one agent (or
 * workflow) execution and is discarded afterwards. It holds the working set an agent accumulates
 * while reasoning — the originating request, gathered context, intermediate tool results and prompt
 * metadata — so later steps in a multi-step flow can read what earlier steps produced without
 * re-fetching.
 *
 * <p>This is intentionally <strong>not</strong> persistent. Durable / cross-session conversation
 * memory belongs to a later phase; nothing here is written to a database. The class is not
 * thread-safe: a single execution is single-threaded through its agent.
 */
public class AgentMemory {

  private final UUID executionId;
  private final Map<String, Object> workingSet = new LinkedHashMap<>();

  private AgentMemory(UUID executionId) {
    this.executionId = executionId;
  }

  /** Creates a fresh memory for a new execution. */
  public static AgentMemory forExecution(UUID executionId) {
    return new AgentMemory(executionId);
  }

  /** Correlation id tying together all steps and telemetry of a single execution. */
  public UUID getExecutionId() {
    return executionId;
  }

  /** Stores a value under a key, overwriting any previous value. Returns {@code this} to chain. */
  public AgentMemory put(String key, Object value) {
    workingSet.put(key, value);
    return this;
  }

  /** Retrieves a value, typed to the caller's expectation. */
  @SuppressWarnings("unchecked")
  public <T> Optional<T> get(String key) {
    return Optional.ofNullable((T) workingSet.get(key));
  }

  /** Records the result of an executed tool for downstream steps to consume. */
  public AgentMemory recordToolResult(String toolName, Object result) {
    workingSet.put("tool:" + toolName, result);
    return this;
  }

  /** Reads back a previously recorded tool result. */
  public <T> Optional<T> toolResult(String toolName) {
    return get("tool:" + toolName);
  }

  /** Immutable snapshot of the working set, primarily for logging/observability. */
  public Map<String, Object> snapshot() {
    return Map.copyOf(workingSet);
  }
}

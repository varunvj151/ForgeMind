package com.forgemind.modules.ai.agent.dto;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable, provider-agnostic input to any {@link com.forgemind.modules.ai.agent.Agent}.
 *
 * <p>Every agent receives the same request shape. The {@code projectId} scopes the work to a single
 * project (used both for context gathering and authorization). Free-form, agent-specific inputs are
 * carried in {@link #parameters} so the interface never has to change when a new agent needs a new
 * knob (e.g. sprint duration, team size, feature description).
 */
@Getter
@Builder
@ToString
public class AgentRequest {

  /** Project the agent should operate on. Required for context building and authorization. */
  private final UUID projectId;

  /**
   * Agent-specific typed parameters (e.g. {@code featureDescription}, {@code sprintDurationDays}).
   * Never {@code null} — defaults to an empty map.
   */
  @Builder.Default private final Map<String, Object> parameters = Collections.emptyMap();

  /** Reads a parameter as a String, if present and non-blank. */
  public Optional<String> stringParam(String key) {
    Object value = parameters.get(key);
    if (value == null) {
      return Optional.empty();
    }
    String str = value.toString();
    return str.isBlank() ? Optional.empty() : Optional.of(str);
  }

  /** Reads a parameter as an Integer, if present and numeric. */
  public Optional<Integer> intParam(String key) {
    Object value = parameters.get(key);
    if (value instanceof Number number) {
      return Optional.of(number.intValue());
    }
    if (value != null) {
      try {
        return Optional.of(Integer.parseInt(value.toString().trim()));
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}

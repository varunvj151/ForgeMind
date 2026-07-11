package com.forgemind.modules.ai.agent.dto;

import com.forgemind.modules.ai.agent.AgentCapability;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable, uniform envelope returned by every {@link com.forgemind.modules.ai.agent.Agent}.
 *
 * <p>The strongly-typed payload lives in {@link #payload} (a structured DTO — never raw LLM text
 * where structure is possible). Execution telemetry ({@code provider}, {@code model}, token counts,
 * duration) is captured alongside so callers and the observability layer get a consistent view
 * regardless of which agent ran.
 *
 * @param <T> the concrete payload type produced by the agent
 */
@Getter
@Builder
@ToString
public class AgentResponse<T> {

  /** The capability that produced this response. */
  private final AgentCapability capability;

  /** The structured result of the agent's work. */
  private final T payload;

  /** Provider that served the underlying LLM call (e.g. GEMINI, MOCK). */
  private final String provider;

  /** Model that served the underlying LLM call. */
  private final String model;

  /** Total tokens consumed by the agent's LLM call(s). */
  @Builder.Default private final int totalTokens = 0;

  /** Wall-clock execution time of the agent in milliseconds. */
  @Builder.Default private final long executionTimeMs = 0L;

  /** Free-form execution metadata (e.g. executionId, tools executed) — never sensitive content. */
  @Builder.Default private final Map<String, Object> metadata = Collections.emptyMap();
}

package com.forgemind.modules.ai.observability;

import com.forgemind.modules.ai.agent.AgentCapability;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Micrometer-backed observability for the agent subsystem.
 *
 * <p>Records which agent ran, how long it took, which provider served it, how many tools it
 * executed, and whether it succeeded, failed or was retried. Deliberately records only
 * <em>metadata</em> (capability names, provider names, tool names, durations, counts) — never prompt
 * text or project content — so no sensitive information leaks into the metrics backend.
 *
 * <p>Metric names are namespaced under {@code ai.agent.*} and complement the existing
 * {@code ai.provider.*} meters emitted by the providers.
 */
@Component
@RequiredArgsConstructor
public class AgentMetrics {

  private final MeterRegistry meterRegistry;

  /** Starts a timing sample for an agent execution. */
  public Timer.Sample startTimer() {
    return Timer.start(meterRegistry);
  }

  /** Records a successful execution's duration, provider and invocation count. */
  public void recordSuccess(
      Timer.Sample sample, AgentCapability capability, String provider, long executionTimeMs) {
    sample.stop(
        meterRegistry.timer(
            "ai.agent.execution.duration",
            "capability",
            capability.name(),
            "provider",
            provider,
            "outcome",
            "success"));
    meterRegistry
        .counter("ai.agent.execution.success", "capability", capability.name(), "provider", provider)
        .increment();
  }

  /** Records a failed execution's duration and reason (exception simple name — never a message). */
  public void recordFailure(Timer.Sample sample, AgentCapability capability, String reason) {
    sample.stop(
        meterRegistry.timer(
            "ai.agent.execution.duration",
            "capability",
            capability.name(),
            "provider",
            "n/a",
            "outcome",
            "failure"));
    meterRegistry
        .counter("ai.agent.execution.failure", "capability", capability.name(), "reason", reason)
        .increment();
  }

  /** Records that a tool was executed on behalf of an agent. */
  public void recordToolExecution(String toolName, long durationMs) {
    meterRegistry
        .timer("ai.agent.tool.duration", "tool", toolName)
        .record(durationMs, TimeUnit.MILLISECONDS);
    meterRegistry.counter("ai.agent.tool.executions", "tool", toolName).increment();
  }

  /** Records a retry of an agent execution. */
  public void recordRetry(AgentCapability capability) {
    meterRegistry.counter("ai.agent.execution.retries", "capability", capability.name()).increment();
  }
}

package com.forgemind.modules.ai.tools;

import com.forgemind.modules.ai.observability.AgentMetrics;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Single entry point through which agents execute tools.
 *
 * <p>Agents are forbidden from instantiating tools directly. Instead they hand a tool name and input
 * to the executor, which:
 *
 * <ul>
 *   <li>owns the registry of all {@link AITool} beans (auto-discovered via Spring),
 *   <li>validates that the requested tool exists,
 *   <li>invokes it and returns its structured output,
 *   <li>records tool telemetry through {@link AgentMetrics}.
 * </ul>
 *
 * <p>Centralising execution keeps cross-cutting concerns (metrics, error wrapping, and any future
 * concerns such as tracing or rate-limiting) in one place instead of scattered across agents.
 */
@Slf4j
@Component
public class ToolExecutor {

  private final Map<String, AITool<?, ?>> toolsByName;
  private final AgentMetrics agentMetrics;

  /**
   * Spring injects every {@link AITool} bean on the classpath. Tools self-register by their {@link
   * AITool#name()}; duplicate names fail fast at startup rather than silently shadowing.
   */
  public ToolExecutor(List<AITool<?, ?>> tools, AgentMetrics agentMetrics) {
    this.agentMetrics = agentMetrics;
    this.toolsByName =
        tools.stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    AITool::name,
                    Function.identity(),
                    (a, b) -> {
                      throw new IllegalStateException(
                          "Duplicate AITool name detected: '" + a.name() + "'");
                    }));
    log.info("ToolExecutor initialized with {} tools: {}", toolsByName.size(), toolsByName.keySet());
  }

  /**
   * Executes a registered tool by name.
   *
   * @param toolName the {@link AITool#name()} to run
   * @param input typed input for the tool
   * @param <I> input type
   * @param <O> output type
   * @return the tool's structured output
   * @throws ToolExecutionException if the tool is unknown or throws while executing
   */
  @SuppressWarnings("unchecked")
  public <I, O> O execute(String toolName, I input) {
    AITool<I, O> tool = (AITool<I, O>) toolsByName.get(toolName);
    if (tool == null) {
      throw new ToolExecutionException(
          "Unknown tool '" + toolName + "'. Registered tools: " + toolsByName.keySet());
    }

    long start = System.currentTimeMillis();
    try {
      O output = tool.execute(input);
      long duration = System.currentTimeMillis() - start;
      agentMetrics.recordToolExecution(toolName, duration);
      log.debug("Tool '{}' executed in {}ms", toolName, duration);
      return output;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - start;
      agentMetrics.recordToolExecution(toolName, duration);
      log.error("Tool '{}' failed after {}ms", toolName, duration, e);
      throw new ToolExecutionException("Tool '" + toolName + "' failed to execute", e);
    }
  }

  /** Whether a tool with the given name is registered. */
  public boolean hasTool(String toolName) {
    return toolsByName.containsKey(toolName);
  }

  /** Names of all registered tools (for diagnostics). */
  public java.util.Set<String> registeredToolNames() {
    return toolsByName.keySet();
  }
}

package com.forgemind.modules.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.forgemind.modules.ai.observability.AgentMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ToolExecutor} registration, lookup and error wrapping. */
class ToolExecutorTest {

  private AgentMetrics agentMetrics;

  /** A trivial echo tool used to exercise the executor. */
  private static class EchoTool implements AITool<String, String> {
    @Override
    public String name() {
      return "echo";
    }

    @Override
    public String description() {
      return "returns its input";
    }

    @Override
    public String execute(String input) {
      return "echo:" + input;
    }
  }

  /** A tool that always fails, to verify error wrapping. */
  private static class BoomTool implements AITool<String, String> {
    @Override
    public String name() {
      return "boom";
    }

    @Override
    public String description() {
      return "always throws";
    }

    @Override
    public String execute(String input) {
      throw new IllegalStateException("kaboom");
    }
  }

  @BeforeEach
  void setUp() {
    agentMetrics = new AgentMetrics(new SimpleMeterRegistry());
  }

  @Test
  void execute_ShouldRunRegisteredTool() {
    ToolExecutor executor = new ToolExecutor(List.of(new EchoTool()), agentMetrics);

    String result = executor.execute("echo", "hello");

    assertEquals("echo:hello", result);
    assertTrue(executor.hasTool("echo"));
  }

  @Test
  void execute_ShouldThrowForUnknownTool() {
    ToolExecutor executor = new ToolExecutor(List.of(new EchoTool()), agentMetrics);

    assertFalse(executor.hasTool("missing"));
    assertThrows(ToolExecutionException.class, () -> executor.execute("missing", "x"));
  }

  @Test
  void execute_ShouldWrapToolFailures() {
    ToolExecutor executor = new ToolExecutor(List.of(new BoomTool()), agentMetrics);

    ToolExecutionException ex =
        assertThrows(ToolExecutionException.class, () -> executor.execute("boom", "x"));
    assertTrue(ex.getMessage().contains("boom"));
  }

  @Test
  void construction_ShouldFailOnDuplicateToolNames() {
    assertThrows(
        IllegalStateException.class,
        () -> new ToolExecutor(List.of(new EchoTool(), new EchoTool()), agentMetrics));
  }
}

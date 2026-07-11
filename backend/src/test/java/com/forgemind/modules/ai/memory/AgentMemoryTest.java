package com.forgemind.modules.ai.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for the short-term {@link AgentMemory} scratchpad. */
class AgentMemoryTest {

  @Test
  void put_And_Get_ShouldRoundTripTypedValues() {
    AgentMemory memory = AgentMemory.forExecution(UUID.randomUUID());
    memory.put("count", 3).put("name", "planner");

    Optional<Integer> count = memory.get("count");
    Optional<String> name = memory.get("name");

    assertEquals(3, count.orElseThrow());
    assertEquals("planner", name.orElseThrow());
  }

  @Test
  void toolResult_ShouldBeNamespacedAndRetrievable() {
    AgentMemory memory = AgentMemory.forExecution(UUID.randomUUID());
    memory.recordToolResult("task.list", java.util.List.of("a", "b"));

    Optional<java.util.List<String>> stored = memory.toolResult("task.list");

    assertEquals(2, stored.orElseThrow().size());
    // Snapshot exposes the namespaced key.
    assertTrue(memory.snapshot().containsKey("tool:task.list"));
  }

  @Test
  void get_ShouldReturnEmptyForMissingKey() {
    AgentMemory memory = AgentMemory.forExecution(UUID.randomUUID());
    assertTrue(memory.get("nope").isEmpty());
  }
}

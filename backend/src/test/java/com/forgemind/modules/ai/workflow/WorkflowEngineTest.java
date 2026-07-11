package com.forgemind.modules.ai.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.forgemind.modules.ai.agent.Agent;
import com.forgemind.modules.ai.agent.AgentCapability;
import com.forgemind.modules.ai.agent.AgentRegistry;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WorkflowEngine}: sequential execution, context threading between steps, and
 * result aggregation. Uses fake agents so nothing touches an LLM.
 */
class WorkflowEngineTest {

  /** Fake agent that echoes a fixed payload and records that it ran. */
  private static class RecordingAgent implements Agent<String> {
    private final AgentCapability capability;
    private final String payload;
    private final AtomicInteger counter;

    RecordingAgent(AgentCapability capability, String payload, AtomicInteger counter) {
      this.capability = capability;
      this.payload = payload;
      this.counter = counter;
    }

    @Override
    public AgentCapability capability() {
      return capability;
    }

    @Override
    public AgentResponse<String> execute(AgentRequest request) {
      counter.incrementAndGet();
      return AgentResponse.<String>builder().capability(capability).payload(payload).build();
    }
  }

  @Test
  void execute_ShouldRunStepsInOrderAndAggregateResults() {
    AtomicInteger calls = new AtomicInteger();
    AgentRegistry registry =
        new AgentRegistry(
            List.of(
                new RecordingAgent(AgentCapability.PLANNING, "plan", calls),
                new RecordingAgent(AgentCapability.DOCUMENTATION, "doc", calls)));
    WorkflowEngine engine = new WorkflowEngine(registry);

    Workflow workflow =
        Workflow.builder("plan-then-doc")
            .step(
                WorkflowStep.of(
                    "plan", AgentCapability.PLANNING, AgentRequest.builder().build()))
            .step(
                WorkflowStep.of(
                    "document", AgentCapability.DOCUMENTATION, AgentRequest.builder().build()))
            .build();

    WorkflowResult result = engine.execute(workflow);

    assertEquals(2, calls.get());
    assertEquals(List.of("plan", "document"), result.executedSteps());
    assertEquals("plan", result.stepResults().get("plan").getPayload());
    assertEquals("doc", result.stepResults().get("document").getPayload());
  }

  @Test
  void execute_ShouldThreadPriorResultsIntoLaterSteps() {
    AtomicInteger calls = new AtomicInteger();
    AgentRegistry registry =
        new AgentRegistry(
            List.of(
                new RecordingAgent(AgentCapability.PLANNING, "feature-x", calls),
                new RecordingAgent(AgentCapability.DOCUMENTATION, "doc", calls)));
    WorkflowEngine engine = new WorkflowEngine(registry);

    // The second step's request factory reads the first step's payload from the context.
    StringBuilder observed = new StringBuilder();
    Workflow workflow =
        Workflow.builder("chained")
            .step(
                WorkflowStep.of(
                    "plan", AgentCapability.PLANNING, AgentRequest.builder().build()))
            .step(
                new WorkflowStep(
                    "document",
                    AgentCapability.DOCUMENTATION,
                    ctx -> {
                      ctx.<String>payloadOf("plan").ifPresent(observed::append);
                      return AgentRequest.builder().build();
                    }))
            .build();

    engine.execute(workflow);

    assertTrue(observed.toString().contains("feature-x"));
  }
}

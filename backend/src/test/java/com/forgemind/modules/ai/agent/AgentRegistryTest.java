package com.forgemind.modules.ai.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AgentRegistry} routing and duplicate-detection behaviour. */
class AgentRegistryTest {

  /** Minimal fake agent so we can test the registry without the full agent machinery. */
  private static class FakeAgent implements Agent<String> {
    private final AgentCapability capability;

    FakeAgent(AgentCapability capability) {
      this.capability = capability;
    }

    @Override
    public AgentCapability capability() {
      return capability;
    }

    @Override
    public AgentResponse<String> execute(AgentRequest request) {
      return AgentResponse.<String>builder().capability(capability).payload("ok").build();
    }
  }

  @Test
  void resolve_ShouldReturnAgentForRegisteredCapability() {
    FakeAgent planner = new FakeAgent(AgentCapability.PLANNING);
    FakeAgent risk = new FakeAgent(AgentCapability.RISK_ANALYSIS);
    AgentRegistry registry = new AgentRegistry(List.of(planner, risk));

    assertSame(planner, registry.resolve(AgentCapability.PLANNING));
    assertSame(risk, registry.resolve(AgentCapability.RISK_ANALYSIS));
  }

  @Test
  void supports_ShouldReflectRegisteredCapabilities() {
    AgentRegistry registry = new AgentRegistry(List.of(new FakeAgent(AgentCapability.STANDUP)));

    assertTrue(registry.supports(AgentCapability.STANDUP));
    assertFalse(registry.supports(AgentCapability.PLANNING));
  }

  @Test
  void resolve_ShouldThrowForUnregisteredCapability() {
    AgentRegistry registry = new AgentRegistry(List.of(new FakeAgent(AgentCapability.STANDUP)));

    assertThrows(
        IllegalArgumentException.class, () -> registry.resolve(AgentCapability.DOCUMENTATION));
  }

  @Test
  void construction_ShouldFailFastOnDuplicateCapability() {
    List<Agent<?>> agents =
        List.of(new FakeAgent(AgentCapability.PLANNING), new FakeAgent(AgentCapability.PLANNING));

    assertThrows(IllegalStateException.class, () -> new AgentRegistry(agents));
  }
}

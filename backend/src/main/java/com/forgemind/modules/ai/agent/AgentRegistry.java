package com.forgemind.modules.ai.agent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Locates the {@link Agent} that fulfils a requested {@link AgentCapability}.
 *
 * <p>This is the seam that eliminates {@code if (capability == PLANNING) …} chains across the
 * codebase. Spring injects every {@code Agent} bean; the registry indexes them by capability at
 * startup. Callers ask for a capability and get the right agent — routing is data, not branching.
 *
 * <p>Registering two agents for the same capability is a configuration error and fails fast at
 * startup, guaranteeing a single, unambiguous handler per capability.
 */
@Slf4j
@Component
public class AgentRegistry {

  private final Map<AgentCapability, Agent<?>> agentsByCapability =
      new EnumMap<>(AgentCapability.class);

  public AgentRegistry(List<Agent<?>> agents) {
    for (Agent<?> agent : agents) {
      Agent<?> previous = agentsByCapability.putIfAbsent(agent.capability(), agent);
      if (previous != null) {
        throw new IllegalStateException(
            "Two agents registered for capability "
                + agent.capability()
                + ": "
                + previous.getClass().getSimpleName()
                + " and "
                + agent.getClass().getSimpleName());
      }
    }
    log.info(
        "AgentRegistry initialized with {} agents: {}",
        agentsByCapability.size(),
        agentsByCapability.keySet());
  }

  /**
   * Resolves the agent for a capability.
   *
   * @throws IllegalArgumentException if no agent is registered for the capability
   */
  @SuppressWarnings("unchecked")
  public <T> Agent<T> resolve(AgentCapability capability) {
    Agent<?> agent = agentsByCapability.get(capability);
    if (agent == null) {
      throw new IllegalArgumentException("No agent registered for capability: " + capability);
    }
    return (Agent<T>) agent;
  }

  /** Whether an agent exists for the given capability. */
  public boolean supports(AgentCapability capability) {
    return agentsByCapability.containsKey(capability);
  }

  /** All capabilities currently served by a registered agent. */
  public Set<AgentCapability> supportedCapabilities() {
    return Set.copyOf(agentsByCapability.keySet());
  }
}

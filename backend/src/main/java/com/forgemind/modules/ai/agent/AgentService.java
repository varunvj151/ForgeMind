package com.forgemind.modules.ai.agent;

import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.Set;

/**
 * Application-facing entry point to the agent subsystem.
 *
 * <p>Controllers and other modules depend on this narrow contract — they name a
 * {@link AgentCapability} and pass an {@link AgentRequest}; the implementation resolves and runs the
 * right agent. This is the boundary the design principle "business services must communicate only
 * through the agent interface / registry" is enforced at.
 */
public interface AgentService {

  /**
   * Runs the agent registered for {@code capability}.
   *
   * @param capability which agent responsibility to invoke
   * @param request uniform agent input
   * @param <T> expected payload type
   * @return the agent's structured response
   * @throws IllegalArgumentException if no agent serves the capability
   */
  <T> AgentResponse<T> invoke(AgentCapability capability, AgentRequest request);

  /** Capabilities currently served by a registered agent. */
  Set<AgentCapability> availableCapabilities();
}

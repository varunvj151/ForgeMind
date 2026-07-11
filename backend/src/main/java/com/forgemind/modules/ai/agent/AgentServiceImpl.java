package com.forgemind.modules.ai.agent;

import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default {@link AgentService}: a thin façade over {@link AgentRegistry}.
 *
 * <p>It deliberately holds no per-agent logic — routing is delegated to the registry, and each agent
 * owns its own validation, authorization, tool use and prompting. This keeps the façade stable as
 * agents are added or changed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

  private final AgentRegistry agentRegistry;

  @Override
  public <T> AgentResponse<T> invoke(AgentCapability capability, AgentRequest request) {
    Agent<T> agent = agentRegistry.resolve(capability);
    return agent.execute(request);
  }

  @Override
  public Set<AgentCapability> availableCapabilities() {
    return agentRegistry.supportedCapabilities();
  }
}

package com.forgemind.modules.ai.agent;

import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;

/**
 * The single contract every specialized AI agent implements.
 *
 * <p>An agent encapsulates one reasoning responsibility (planning, documentation, risk analysis,
 * …). It accepts a uniform {@link AgentRequest}, and — behind the scenes — validates its input,
 * asserts the caller's authorization, gathers the context it needs via tools, builds a prompt
 * (reusing {@code PromptTemplateManager}), calls the shared {@code AiProvider}, and returns a
 * structured {@link AgentResponse}.
 *
 * <p><strong>Business services depend only on this interface</strong> (resolved through the
 * {@link AgentRegistry} by {@link AgentCapability}), never on concrete agent classes. Adding a new
 * agent is therefore: implement {@code Agent}, declare its capability, register it as a Spring bean.
 * No existing orchestration code changes.
 *
 * @param <T> the structured payload type this agent produces
 */
public interface Agent<T> {

  /** The capability this agent fulfils. Used by the registry to route requests. */
  AgentCapability capability();

  /**
   * Executes the agent's end-to-end workflow for the given request.
   *
   * @param request uniform, validated-on-entry request
   * @return a structured response envelope with payload and execution telemetry
   */
  AgentResponse<T> execute(AgentRequest request);
}

package com.forgemind.modules.ai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.exception.AiProviderException;
import com.forgemind.modules.ai.memory.AgentMemory;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.tools.ToolExecutor;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Template-method base shared by every concrete agent.
 *
 * <p>It fixes the invariant lifecycle so individual agents only fill in the parts that differ:
 *
 * <ol>
 *   <li><b>validate</b> the request ({@link #validate(AgentRequest)}),
 *   <li><b>authorize</b> the caller against the project ({@link AgentAccessGuard}),
 *   <li><b>reason</b> — gather context via tools, build a prompt, and produce a structured payload
 *       ({@link #doExecute(AgentRequest, AgentMemory)}),
 *   <li><b>wrap</b> the payload with execution telemetry, recording success/failure metrics.
 * </ol>
 *
 * <p>Subclasses get helper access to the shared collaborators (provider, prompt manager, tool
 * executor, object mapper) and to a per-execution {@link AgentMemory} scratchpad. This removes
 * boilerplate and guarantees no agent can skip authorization or metrics.
 *
 * @param <T> the structured payload type produced by the agent
 */
@Slf4j
public abstract class AbstractAgent<T> implements Agent<T> {

  protected final AiProvider aiProvider;
  protected final PromptTemplateManager promptTemplateManager;
  protected final ToolExecutor toolExecutor;
  protected final ObjectMapper objectMapper;
  private final AgentAccessGuard accessGuard;
  private final AgentMetrics agentMetrics;

  protected AbstractAgent(
      AiProvider aiProvider,
      PromptTemplateManager promptTemplateManager,
      ToolExecutor toolExecutor,
      ObjectMapper objectMapper,
      AgentAccessGuard accessGuard,
      AgentMetrics agentMetrics) {
    this.aiProvider = aiProvider;
    this.promptTemplateManager = promptTemplateManager;
    this.toolExecutor = toolExecutor;
    this.objectMapper = objectMapper;
    this.accessGuard = accessGuard;
    this.agentMetrics = agentMetrics;
  }

  /**
   * Fixed execution skeleton. Not overridable — subclasses customise via {@link #validate} and
   * {@link #doExecute}.
   */
  @Override
  public final AgentResponse<T> execute(AgentRequest request) {
    UUID executionId = deriveExecutionId(request);
    AgentMemory memory = AgentMemory.forExecution(executionId);
    memory.put("request", request);

    Timer.Sample sample = agentMetrics.startTimer();
    long start = System.currentTimeMillis();

    log.info("Agent[{}] starting execution {}", capability(), executionId);
    try {
      // 1. Input validation — fail fast before any expensive work.
      validate(request);

      // 2. Authorization — never gather or expose data the caller cannot access.
      if (request.getProjectId() != null) {
        accessGuard.requireProjectAccess(request.getProjectId());
      }

      // 3. Reasoning — subclass gathers context via tools, prompts the provider, structures output.
      ExecutionResult<T> result = doExecute(request, memory);

      long elapsed = System.currentTimeMillis() - start;
      String provider = result.aiResponse() != null ? result.aiResponse().getProvider() : "n/a";
      agentMetrics.recordSuccess(sample, capability(), provider, elapsed);
      log.info("Agent[{}] completed execution {} in {}ms", capability(), executionId, elapsed);

      return AgentResponse.<T>builder()
          .capability(capability())
          .payload(result.payload())
          .provider(provider)
          .model(result.aiResponse() != null ? result.aiResponse().getModel() : null)
          .totalTokens(result.aiResponse() != null ? result.aiResponse().getTotalTokens() : 0)
          .executionTimeMs(elapsed)
          .metadata(java.util.Map.<String, Object>of("executionId", executionId.toString()))
          .build();

    } catch (RuntimeException e) {
      agentMetrics.recordFailure(sample, capability(), e.getClass().getSimpleName());
      log.error("Agent[{}] failed execution {}: {}", capability(), executionId, e.getMessage());
      throw e;
    }
  }

  // ── Extension points ───────────────────────────────────────────────────────

  /**
   * Validates agent-specific inputs. Default: nothing beyond the standard checks. Subclasses should
   * throw {@link IllegalArgumentException} for bad input.
   */
  protected void validate(AgentRequest request) {
    // no-op by default
  }

  /**
   * The agent's core reasoning: gather context (via {@link #toolExecutor}), build a prompt (via
   * {@link #promptTemplateManager}), call {@link #aiProvider}, and return a structured payload plus
   * the raw {@link AiResponse} (for telemetry). Implementations should record intermediate results
   * in {@code memory} so multi-step flows can inspect them.
   */
  protected abstract ExecutionResult<T> doExecute(AgentRequest request, AgentMemory memory);

  // ── Shared helpers ─────────────────────────────────────────────────────────

  /** Calls the shared provider with a system + user prompt, in text or JSON mode. */
  protected AiResponse callProvider(String systemPrompt, String userPrompt, boolean jsonMode) {
    AiRequest aiRequest =
        AiRequest.builder()
            .systemPrompt(systemPrompt)
            .userPrompt(userPrompt)
            .jsonMode(jsonMode)
            .build();
    return aiProvider.generate(aiRequest);
  }

  /**
   * Parses a JSON array response from the LLM into a typed list, tolerating markdown code fences.
   * Mirrors the parsing already used by the legacy orchestrator so behaviour is consistent.
   */
  protected <E> List<E> parseJsonArray(String content, TypeReference<List<E>> typeReference) {
    try {
      String clean = stripCodeFences(content);
      return objectMapper.readValue(clean, typeReference);
    } catch (Exception e) {
      log.error("Agent[{}] failed to parse JSON array from AI response", capability(), e);
      throw new AiProviderException("AI returned invalid JSON array for " + capability(), e);
    }
  }

  /** Parses a single JSON object response from the LLM into a typed object. */
  protected <E> E parseJsonObject(String content, Class<E> type) {
    try {
      String clean = stripCodeFences(content);
      return objectMapper.readValue(clean, type);
    } catch (Exception e) {
      log.error("Agent[{}] failed to parse JSON object from AI response", capability(), e);
      throw new AiProviderException("AI returned invalid JSON object for " + capability(), e);
    }
  }

  private String stripCodeFences(String content) {
    if (content == null) {
      return "";
    }
    return content.replaceAll("```json", "").replaceAll("```", "").trim();
  }

  private UUID deriveExecutionId(AgentRequest request) {
    // Deterministic-free id is acceptable here; execution ids are ephemeral and only correlate logs.
    return UUID.randomUUID();
  }

  /**
   * Internal carrier tying an agent's structured payload to the raw provider response that produced
   * it, so the base class can populate telemetry uniformly.
   */
  protected record ExecutionResult<T>(T payload, AiResponse aiResponse) {}
}

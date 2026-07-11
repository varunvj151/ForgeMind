package com.forgemind.modules.ai.workflow;

import com.forgemind.modules.ai.agent.Agent;
import com.forgemind.modules.ai.agent.AgentRegistry;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Lightweight, deterministic orchestration engine for multi-step, multi-agent flows.
 *
 * <p>Given a {@link Workflow} (pure data) it executes each {@link WorkflowStep} in order:
 *
 * <pre>
 *   for each step:
 *     agent = registry.resolve(step.capability)      // routing, not branching
 *     request = step.requestFactory(context)         // parameterised by prior results
 *     response = agent.execute(request)              // validate → authorize → tools → prompt → provider
 *     context.record(step.name, response)            // feed the next step
 * </pre>
 *
 * <p>The engine knows nothing about specific agents or tools — it depends only on the
 * {@link AgentRegistry} and the {@link Agent} contract. New workflows are added as data; the engine
 * never changes. Each agent still enforces its own validation and authorization, so orchestration
 * cannot bypass security.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEngine {

  private final AgentRegistry agentRegistry;

  /** Executes a workflow starting from a fresh context. */
  public WorkflowResult execute(Workflow workflow) {
    return execute(workflow, new WorkflowContext());
  }

  /** Executes a workflow with a caller-seeded context (e.g. pre-set attributes). */
  public WorkflowResult execute(Workflow workflow, WorkflowContext context) {
    long start = System.currentTimeMillis();
    List<String> executed = new ArrayList<>();

    log.info(
        "Workflow[{}] starting with {} step(s)", workflow.name(), workflow.steps().size());

    for (WorkflowStep step : workflow.steps()) {
      log.info("Workflow[{}] → step '{}' ({})", workflow.name(), step.name(), step.capability());

      Agent<Object> agent = agentRegistry.resolve(step.capability());
      AgentRequest request = step.requestFactory().build(context);
      AgentResponse<Object> response = agent.execute(request);

      context.recordStepResult(step.name(), response);
      executed.add(step.name());
    }

    long elapsed = System.currentTimeMillis() - start;
    log.info("Workflow[{}] completed {} step(s) in {}ms", workflow.name(), executed.size(), elapsed);

    return new WorkflowResult(workflow.name(), executed, context.allResults(), elapsed);
  }
}

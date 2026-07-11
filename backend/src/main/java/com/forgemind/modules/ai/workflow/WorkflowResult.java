package com.forgemind.modules.ai.workflow;

import com.forgemind.modules.ai.agent.dto.AgentResponse;
import java.util.List;
import java.util.Map;

/**
 * Aggregate outcome of a {@link Workflow} run: every step's response, in order, plus total wall-clock
 * time. Callers can read individual step payloads by name.
 */
public record WorkflowResult(
    String workflowName,
    List<String> executedSteps,
    Map<String, AgentResponse<?>> stepResults,
    long totalExecutionTimeMs) {}

package com.forgemind.modules.ai.agent;

import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import com.forgemind.modules.ai.agent.dto.DocumentationResult;
import com.forgemind.modules.ai.agent.dto.PlanResult;
import com.forgemind.modules.ai.agent.dto.RiskAnalysisResult;
import com.forgemind.modules.ai.agent.dto.SprintPlanResult;
import com.forgemind.modules.ai.agent.dto.StandupResult;
import com.forgemind.modules.ai.dto.request.AgentPlanRequest;
import com.forgemind.modules.ai.dto.request.AgentSprintRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for the AI agent subsystem.
 *
 * <p>Mounted under a dedicated {@code /agents} sub-path so the original AI endpoints
 * ({@code /api/v1/ai/projects/...}) remain untouched and backward-compatible. Every endpoint routes
 * through {@link AgentService} by {@link AgentCapability}; the controller contains no agent-specific
 * logic beyond mapping HTTP inputs to an {@link AgentRequest}.
 *
 * <p>Security is enforced inside each agent (via the access guard), on top of the global
 * authentication requirement for all {@code /api/v1/**} routes.
 */
@RestController
@RequestMapping("/api/v1/ai/agents")
@RequiredArgsConstructor
public class AgentController {

  private final AgentService agentService;

  /** Lists the capabilities currently served by a registered agent. */
  @GetMapping("/capabilities")
  public Set<AgentCapability> capabilities() {
    return agentService.availableCapabilities();
  }

  /** Planner: decompose a feature request into a structured delivery plan. */
  @PostMapping("/projects/{projectId}/plan")
  public AgentResponse<PlanResult> plan(
      @PathVariable UUID projectId, @Valid @RequestBody AgentPlanRequest body) {
    AgentRequest request =
        AgentRequest.builder()
            .projectId(projectId)
            .parameters(
                Map.<String, Object>of(PlannerAgent.PARAM_FEATURE, body.getFeatureDescription()))
            .build();
    return agentService.invoke(AgentCapability.PLANNING, request);
  }

  /** Risk analysis: score and explain the project's delivery risk. */
  @GetMapping("/projects/{projectId}/risk-analysis")
  public AgentResponse<RiskAnalysisResult> riskAnalysis(@PathVariable UUID projectId) {
    AgentRequest request = AgentRequest.builder().projectId(projectId).build();
    return agentService.invoke(AgentCapability.RISK_ANALYSIS, request);
  }

  /** Sprint planning: build an ordered sprint backlog for the given duration and team size. */
  @PostMapping("/projects/{projectId}/sprint-plan")
  public AgentResponse<SprintPlanResult> sprintPlan(
      @PathVariable UUID projectId, @Valid @RequestBody AgentSprintRequest body) {
    AgentRequest request =
        AgentRequest.builder()
            .projectId(projectId)
            .parameters(
                Map.<String, Object>of(
                    SprintPlanningAgent.PARAM_DURATION_DAYS, body.getSprintDurationDays(),
                    SprintPlanningAgent.PARAM_TEAM_SIZE, body.getTeamSize()))
            .build();
    return agentService.invoke(AgentCapability.SPRINT_PLANNING, request);
  }

  /** Stand-up: generate yesterday / today / blockers / upcoming from recent activity. */
  @GetMapping("/projects/{projectId}/standup")
  public AgentResponse<StandupResult> standup(@PathVariable UUID projectId) {
    AgentRequest request = AgentRequest.builder().projectId(projectId).build();
    return agentService.invoke(AgentCapability.STANDUP, request);
  }

  /** Documentation: generate a document of the requested type (defaults to README). */
  @GetMapping("/projects/{projectId}/documentation")
  public AgentResponse<DocumentationResult> documentation(
      @PathVariable UUID projectId,
      @RequestParam(name = "type", defaultValue = "README") String type) {
    AgentRequest request =
        AgentRequest.builder()
            .projectId(projectId)
            .parameters(Map.<String, Object>of(DocumentationAgent.PARAM_DOC_TYPE, type))
            .build();
    return agentService.invoke(AgentCapability.DOCUMENTATION, request);
  }
}

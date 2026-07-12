package com.forgemind.modules.ai.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import com.forgemind.modules.ai.agent.dto.PlanResult;
import com.forgemind.modules.ai.agent.security.AgentAccessGuard;
import com.forgemind.modules.ai.context.AiContextBuilder;
import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.rag.RagContext;
import com.forgemind.modules.ai.rag.RagOrchestrator;
import com.forgemind.modules.ai.tools.ToolExecutor;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.entity.ProjectStatus;
import com.forgemind.modules.project.service.ProjectService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit test for {@link PlannerAgent}: validation, context reuse and structured plan parsing. */
@ExtendWith(MockitoExtension.class)
class PlannerAgentTest {

  @Mock private AiProvider aiProvider;
  @Mock private AiContextBuilder contextBuilder;
  @Mock private CurrentUserProvider currentUserProvider;
  @Mock private ProjectService projectService;
  @Mock private ToolExecutor toolExecutor;
  @Mock private RagOrchestrator ragOrchestrator;

  private PlannerAgent agent;
  private final UUID projectId = UUID.randomUUID();
  private final Long userId = 7L;

  @BeforeEach
  void setUp() {
    AgentMetrics metrics = new AgentMetrics(new SimpleMeterRegistry());
    AgentAccessGuard guard = new AgentAccessGuard(currentUserProvider, projectService);
    agent =
        new PlannerAgent(
            aiProvider,
            new PromptTemplateManager(),
            toolExecutor,
            new ObjectMapper(),
            guard,
            metrics,
            contextBuilder,
            ragOrchestrator);
  }

  private void allowAccess() {
    User user = new User();
    user.setId(userId);
    when(currentUserProvider.getCurrentUser()).thenReturn(user);
    when(projectService.getProjectById(projectId))
        .thenReturn(
            new ProjectResponse(
                projectId,
                "P",
                "d",
                ProjectStatus.ACTIVE,
                Instant.now(),
                Instant.now(),
                userId,
                "owner"));
  }

  @Test
  void execute_ShouldReturnStructuredPlan() {
    allowAccess();
    Map<String, Object> ctx = new HashMap<>();
    ctx.put("contextJson", "{}");
    when(contextBuilder.buildProjectContext(projectId)).thenReturn(ctx);
    when(ragOrchestrator.augmentPrompt(eq(projectId), any()))
        .thenReturn(new RagContext("augmented prompt", java.util.List.of(), "augmented prompt", false));

    String json =
        "{\"featureSummary\":\"OAuth login\","
            + "\"milestones\":[{\"name\":\"M1\",\"objective\":\"o\",\"priority\":\"HIGH\",\"estimatedEffort\":\"3d\"}],"
            + "\"tasks\":[{\"title\":\"T1\",\"description\":\"d\",\"priority\":\"HIGH\",\"phase\":\"p\",\"estimatedEffort\":\"1d\",\"dependsOn\":[]}]}";
    when(aiProvider.generate(any(AiRequest.class)))
        .thenReturn(AiResponse.builder().content(json).provider("MOCK").model("m").build());

    AgentRequest request =
        AgentRequest.builder()
            .projectId(projectId)
            .parameters(Map.<String, Object>of(PlannerAgent.PARAM_FEATURE, "Implement OAuth Login"))
            .build();

    AgentResponse<PlanResult> response = agent.execute(request);

    assertEquals("OAuth login", response.getPayload().featureSummary());
    assertEquals(1, response.getPayload().milestones().size());
    assertEquals("T1", response.getPayload().tasks().get(0).title());
    assertEquals(AgentCapability.PLANNING, response.getCapability());
  }

  @Test
  void execute_ShouldRejectMissingFeatureParameter() {
    AgentRequest request = AgentRequest.builder().projectId(projectId).build();
    assertThrows(IllegalArgumentException.class, () -> agent.execute(request));
  }
}

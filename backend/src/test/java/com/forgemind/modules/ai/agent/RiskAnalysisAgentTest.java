package com.forgemind.modules.ai.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import com.forgemind.modules.ai.agent.dto.RiskAnalysisResult;
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
import com.forgemind.modules.ai.tools.impl.ActivityTool;
import com.forgemind.modules.ai.tools.impl.MetricsTool;
import com.forgemind.modules.ai.tools.impl.ProjectTool;
import com.forgemind.modules.ai.tools.impl.TaskTool;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import com.forgemind.modules.task.service.TaskService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

/**
 * Unit test for {@link RiskAnalysisAgent}. Uses a stub {@link AiProvider} (never a real LLM) and
 * verifies tool composition, authorization and structured JSON parsing end-to-end.
 */
@ExtendWith(MockitoExtension.class)
class RiskAnalysisAgentTest {

  @Mock private ProjectService projectService;
  @Mock private TaskService taskService;
  @Mock private ActivityService activityService;
  @Mock private CurrentUserProvider currentUserProvider;
  @Mock private AiProvider aiProvider;
  @Mock private AiContextBuilder contextBuilder;
  @Mock private RagOrchestrator ragOrchestrator;

  private RiskAnalysisAgent agent;
  private final UUID projectId = UUID.randomUUID();
  private final Long userId = 42L;

  @BeforeEach
  void setUp() {
    // Mirror the Spring-managed mapper: register JavaTimeModule so Instant fields serialize.
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    AgentMetrics metrics = new AgentMetrics(new SimpleMeterRegistry());

    ToolExecutor toolExecutor =
        new ToolExecutor(
            List.of(
                new ProjectTool(projectService),
                new TaskTool(taskService),
                new ActivityTool(activityService),
                new MetricsTool()),
            metrics);

    AgentAccessGuard guard = new AgentAccessGuard(currentUserProvider, projectService);

    agent =
        new RiskAnalysisAgent(
            aiProvider, new PromptTemplateManager(), toolExecutor, objectMapper, guard, metrics, contextBuilder, ragOrchestrator);
  }

  private ProjectResponse ownedProject() {
    return new ProjectResponse(
        projectId,
        "Proj",
        "desc",
        com.forgemind.modules.project.entity.ProjectStatus.ACTIVE,
        Instant.now(),
        Instant.now(),
        userId,
        "owner");
  }

  private User currentUser() {
    User u = new User();
    u.setId(userId);
    u.setUsername("owner");
    return u;
  }

  @Test
  void execute_ShouldComposeToolsAndParseStructuredRisk() {
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser());
    when(projectService.getProjectById(projectId)).thenReturn(ownedProject());
    Page<com.forgemind.modules.task.dto.response.TaskResponse> emptyTasks =
        new PageImpl<>(List.of());
    Page<com.forgemind.modules.activity.dto.response.ActivityResponse> emptyActivity =
        new PageImpl<>(List.of());
    when(taskService.listProjectTasks(any(), any())).thenReturn(emptyTasks);
    when(activityService.getProjectActivities(any(), any())).thenReturn(emptyActivity);
    when(ragOrchestrator.augmentPrompt(eq(projectId), any()))
        .thenReturn(new RagContext("augmented prompt", java.util.List.of(), "augmented prompt", false));

    String json =
        "{\"riskScore\":40,\"riskLevel\":\"MEDIUM\",\"summary\":\"ok\","
            + "\"risks\":[{\"level\":\"MEDIUM\",\"area\":\"schedule\",\"description\":\"d\",\"recommendation\":\"r\"}],"
            + "\"recommendations\":[\"a\"],\"nextActions\":[\"b\"]}";
    when(aiProvider.generate(any(AiRequest.class)))
        .thenReturn(AiResponse.builder().content(json).provider("MOCK").model("m").totalTokens(5).build());

    AgentRequest request = AgentRequest.builder().projectId(projectId).build();
    AgentResponse<RiskAnalysisResult> response = agent.execute(request);

    assertNotNull(response.getPayload());
    assertEquals(40, response.getPayload().riskScore());
    assertEquals("MEDIUM", response.getPayload().riskLevel());
    assertEquals(1, response.getPayload().risks().size());
    assertEquals("MOCK", response.getProvider());
  }

  @Test
  void execute_ShouldDenyAccessForNonOwner() {
    User stranger = new User();
    stranger.setId(999L);
    stranger.setUsername("stranger");

    when(currentUserProvider.getCurrentUser()).thenReturn(stranger);
    when(projectService.getProjectById(projectId)).thenReturn(ownedProject());

    AgentRequest request = AgentRequest.builder().projectId(projectId).build();

    assertThrows(AccessDeniedException.class, () -> agent.execute(request));
  }
}

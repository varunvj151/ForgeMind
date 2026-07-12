package com.forgemind.modules.git.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.agent.AgentCapability;
import com.forgemind.modules.ai.agent.dto.AgentRequest;
import com.forgemind.modules.ai.agent.dto.AgentResponse;
import com.forgemind.modules.ai.observability.AgentMetrics;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.provider.MockAiProvider;
import com.forgemind.modules.git.ai.dto.CodeReviewResult;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeReviewAgentTest {

  @Mock private PromptTemplateManager templateManager;
  @Mock private AgentMetrics metrics;
  @Mock private AiProvider aiProvider;
  @Mock private com.forgemind.modules.ai.tools.ToolExecutor toolExecutor;
  @Mock private com.forgemind.modules.ai.agent.security.AgentAccessGuard accessGuard;
  private ObjectMapper objectMapper;
  private CodeReviewAgent agent;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    agent = new CodeReviewAgent(aiProvider, templateManager, toolExecutor, objectMapper, accessGuard, metrics);
  }

  @Test
  void testCapability() {
    assertThat(agent.capability()).isEqualTo(AgentCapability.CODE_REVIEW);
  }

  @Test
  void testExecute() {
    String mockJsonResponse = """
        {
          "score": 85,
          "summary": "Good overall, minor issues.",
          "issues": [
            {
              "severity": "WARNING",
              "category": "Clean Code",
              "file": "Test.java",
              "line": 10,
              "description": "Magic number",
              "recommendation": "Use a constant"
            }
          ],
          "positives": ["Good naming"],
          "overallRecommendations": ["Add more tests"]
        }
        """;
    
    com.forgemind.modules.ai.dto.AiResponse mockAiResponse = com.forgemind.modules.ai.dto.AiResponse.builder()
        .content(mockJsonResponse)
        .build();
        
    when(aiProvider.generate(org.mockito.ArgumentMatchers.any())).thenReturn(mockAiResponse);
    when(templateManager.resolveTemplate(anyString(), org.mockito.ArgumentMatchers.anyMap()))
        .thenReturn("mocked prompt");

    AgentRequest request = AgentRequest.builder()
        .projectId(UUID.randomUUID())
        .parameters(Map.of("codeChunks", "public class Test {}"))
        .build();

    AgentResponse<CodeReviewResult> response = agent.execute(request);

    assertThat(response.getPayload()).isNotNull();
    assertThat(response.getPayload().getScore()).isEqualTo(85);
    assertThat(response.getPayload().getIssues()).hasSize(1);
    assertThat(response.getPayload().getIssues().get(0).getSeverity()).isEqualTo("WARNING");
  }
}

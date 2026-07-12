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
import com.forgemind.modules.git.ai.dto.ArchitectureResult;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArchitectureAgentTest {

  @Mock private PromptTemplateManager templateManager;
  @Mock private AgentMetrics metrics;
  @Mock private AiProvider aiProvider;
  @Mock private com.forgemind.modules.ai.tools.ToolExecutor toolExecutor;
  @Mock private com.forgemind.modules.ai.agent.security.AgentAccessGuard accessGuard;
  private ObjectMapper objectMapper;
  private ArchitectureAgent agent;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    agent = new ArchitectureAgent(aiProvider, templateManager, toolExecutor, objectMapper, accessGuard, metrics);
  }

  @Test
  void testCapability() {
    assertThat(agent.capability()).isEqualTo(AgentCapability.ARCHITECTURE_ANALYSIS);
  }

  @Test
  void testExecute() {
    String mockJsonResponse = """
        {
          "summary": "Clean layered architecture.",
          "modules": [
            {
              "name": "core",
              "description": "Core domain",
              "fileCount": 10
            }
          ],
          "risks": [
             {
               "severity": "LOW",
               "description": "Some tightly coupled services",
               "affectedFiles": ["ServiceA.java"],
               "recommendation": "Use interfaces"
             }
          ],
          "refactoringOpportunities": ["Extract interface"],
          "technicalDebt": "Low",
          "overallHealthScore": 90
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
        .parameters(Map.of("fileStructure", "src/main/java..."))
        .build();

    AgentResponse<ArchitectureResult> response = agent.execute(request);

    assertThat(response.getPayload()).isNotNull();
    assertThat(response.getPayload().getOverallHealthScore()).isEqualTo(90);
    assertThat(response.getPayload().getModules()).hasSize(1);
    assertThat(response.getPayload().getRisks()).hasSize(1);
  }
}

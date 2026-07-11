package com.forgemind.modules.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.context.AiContextBuilder;
import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.dto.response.AiTaskSuggestionResponse;
import com.forgemind.modules.ai.prompt.PromptTemplateManager;
import com.forgemind.modules.ai.provider.AiProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiOrchestratorServiceImplTest {

    @Mock
    private AiProvider aiProvider;

    @Mock
    private AiContextBuilder contextBuilder;

    private PromptTemplateManager promptTemplateManager;
    private ObjectMapper objectMapper;
    private AiOrchestratorServiceImpl aiOrchestratorService;

    @BeforeEach
    void setUp() {
        promptTemplateManager = new PromptTemplateManager();
        objectMapper = new ObjectMapper();
        aiOrchestratorService = new AiOrchestratorServiceImpl(aiProvider, contextBuilder, promptTemplateManager, objectMapper);
    }

    @Test
    void generateTasks_ShouldParseJsonArrayResponse() {
        UUID projectId = UUID.randomUUID();
        when(contextBuilder.buildProjectContext(projectId)).thenReturn(new java.util.HashMap<>(Map.of("contextJson", "{}")));

        String mockJson = "[{\"title\": \"Task 1\", \"description\": \"Desc 1\", \"priority\": \"HIGH\"}]";
        when(aiProvider.generate(any(AiRequest.class))).thenReturn(AiResponse.builder().content(mockJson).build());

        List<AiTaskSuggestionResponse> tasks = aiOrchestratorService.generateTasks(projectId, "Build login feature");

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
        assertEquals("HIGH", tasks.get(0).getPriority());
    }
}

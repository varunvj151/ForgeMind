package com.forgemind.modules.ai.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.service.TaskService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiContextBuilder {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    /**
     * Gathers project and task information to build a context map for prompt injection.
     */
    public Map<String, Object> buildProjectContext(UUID projectId) {
        Map<String, Object> context = new HashMap<>();
        
        try {
            ProjectResponse project = projectService.getProjectById(projectId);
            context.put("project", project);
            
            // Fetch first 50 tasks to give LLM context without blowing up tokens
            Page<TaskResponse> tasksPage = taskService.listProjectTasks(projectId, Pageable.ofSize(50));
            List<TaskResponse> tasks = tasksPage.getContent();
            context.put("tasks", tasks);
            
            // Serialize context to a readable JSON string for easy injection into the prompt
            context.put("contextJson", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    Map.of("project", project, "tasks", tasks)));
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AI context", e);
            context.put("contextJson", "{}");
        } catch (Exception e) {
            log.error("Failed to gather AI context for project {}", projectId, e);
            throw new RuntimeException("Failed to gather context", e);
        }

        return context;
    }
}

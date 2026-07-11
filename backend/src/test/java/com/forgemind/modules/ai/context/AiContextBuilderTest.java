package com.forgemind.modules.ai.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.service.TaskService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AiContextBuilderTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

    private AiContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        contextBuilder = new AiContextBuilder(projectService, taskService, new ObjectMapper());
    }

    @Test
    void buildProjectContext_ShouldGatherProjectAndTasks() {
        UUID projectId = UUID.randomUUID();
        
        ProjectResponse project =
            new ProjectResponse(
                projectId,
                "Test Project",
                "desc",
                com.forgemind.modules.project.entity.ProjectStatus.ACTIVE,
                java.time.Instant.now(),
                java.time.Instant.now(),
                1L,
                "owner");

        TaskResponse task =
            new TaskResponse(
                UUID.randomUUID(),
                projectId,
                "Test Project",
                "Test Task",
                "desc",
                com.forgemind.modules.task.entity.TaskStatus.TODO,
                com.forgemind.modules.task.entity.TaskPriority.MEDIUM,
                null,
                null,
                null,
                java.time.Instant.now(),
                java.time.Instant.now());

        when(projectService.getProjectById(projectId)).thenReturn(project);
        when(taskService.listProjectTasks(eq(projectId), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(task)));

        Map<String, Object> context = contextBuilder.buildProjectContext(projectId);

        assertNotNull(context);
        assertEquals(project, context.get("project"));
        assertEquals(1, ((List<?>) context.get("tasks")).size());
        assertNotNull(context.get("contextJson"));
    }
}

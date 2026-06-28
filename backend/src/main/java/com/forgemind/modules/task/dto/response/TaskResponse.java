package com.forgemind.modules.task.dto.response;

import com.forgemind.modules.task.entity.TaskPriority;
import com.forgemind.modules.task.entity.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        AssigneeInfo assignee,
        AssigneeInfo createdBy,
        Instant dueDate,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Minimal user summary embedded in a task response.
     * Avoids pulling in the full UserResponse with role arrays.
     */
    public record AssigneeInfo(
            Long id,
            String username,
            String firstName,
            String lastName
    ) {}
}

package com.forgemind.modules.task.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskAssigneeRequest(
        // Nullable — allows un-assigning a task by passing null
        Long assigneeId
) {}

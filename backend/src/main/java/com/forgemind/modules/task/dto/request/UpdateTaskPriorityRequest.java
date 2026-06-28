package com.forgemind.modules.task.dto.request;

import com.forgemind.modules.task.entity.TaskPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskPriorityRequest(
        @NotNull(message = "Priority is required")
        TaskPriority priority
) {}

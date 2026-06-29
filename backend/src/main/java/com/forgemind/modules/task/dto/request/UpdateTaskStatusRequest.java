package com.forgemind.modules.task.dto.request;

import com.forgemind.modules.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(@NotNull(message = "Status is required") TaskStatus status) {}

package com.forgemind.modules.task.dto.request;

public record UpdateTaskAssigneeRequest(
    // Nullable — allows un-assigning a task by passing null
    Long assigneeId) {}

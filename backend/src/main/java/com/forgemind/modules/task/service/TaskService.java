package com.forgemind.modules.task.service;

import com.forgemind.modules.task.dto.request.CreateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskAssigneeRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskPriorityRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskStatusRequest;
import com.forgemind.modules.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(UUID projectId, CreateTaskRequest request);

    TaskResponse updateTask(UUID taskId, UpdateTaskRequest request);

    void deleteTask(UUID taskId);

    TaskResponse getTask(UUID taskId);

    TaskResponse changeStatus(UUID taskId, UpdateTaskStatusRequest request);

    TaskResponse changePriority(UUID taskId, UpdateTaskPriorityRequest request);

    TaskResponse assignTask(UUID taskId, UpdateTaskAssigneeRequest request);

    Page<TaskResponse> listProjectTasks(UUID projectId, Pageable pageable);

    Page<TaskResponse> listMyTasks(Pageable pageable);
}

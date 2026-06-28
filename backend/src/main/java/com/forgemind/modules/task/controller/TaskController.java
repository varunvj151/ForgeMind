package com.forgemind.modules.task.controller;

import com.forgemind.common.exception.ErrorResponse;
import com.forgemind.modules.task.dto.request.CreateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskAssigneeRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskPriorityRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskStatusRequest;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    // ── Project-scoped endpoints ──────────────────────────────────────────────

    @Operation(summary = "Create a task in a project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(projectId, request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/tasks/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List all tasks in a project (paginated)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of tasks"),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<Page<TaskResponse>> listProjectTasks(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.listProjectTasks(projectId, pageable));
    }

    // ── Task-scoped endpoints ─────────────────────────────────────────────────

    @Operation(summary = "Get tasks assigned to the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "My tasks"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/tasks/my")
    public ResponseEntity<Page<TaskResponse>> listMyTasks(
            @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return ResponseEntity.ok(taskService.listMyTasks(pageable));
    }

    @Operation(summary = "Get a task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task details"),
            @ApiResponse(responseCode = "403", description = "Not the owner or assignee", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getTask(taskId));
    }

    @Operation(summary = "Update task title, description, and due date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @Operation(summary = "Delete a task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change task status (owner or assignee)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the owner or assignee", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/tasks/{taskId}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.changeStatus(taskId, request));
    }

    @Operation(summary = "Change task priority (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Priority updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/tasks/{taskId}/priority")
    public ResponseEntity<TaskResponse> changePriority(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskPriorityRequest request) {
        return ResponseEntity.ok(taskService.changePriority(taskId, request));
    }

    @Operation(summary = "Assign or un-assign a task (owner only). Pass null assigneeId to un-assign.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignee updated"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task or User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/tasks/{taskId}/assignee")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskAssigneeRequest request) {
        return ResponseEntity.ok(taskService.assignTask(taskId, request));
    }
}

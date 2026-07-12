package com.forgemind.modules.task.service;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.activity.entity.ActivityType;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.repository.ProjectRepository;
import com.forgemind.modules.task.dto.request.CreateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskAssigneeRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskPriorityRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskRequest;
import com.forgemind.modules.task.dto.request.UpdateTaskStatusRequest;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.entity.Task;
import com.forgemind.modules.task.mapper.TaskMapper;
import com.forgemind.modules.task.repository.TaskRepository;
import com.forgemind.modules.ai.indexing.events.TaskCreatedEvent;
import com.forgemind.modules.ai.indexing.events.TaskUpdatedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TaskMapper taskMapper;
  private final CurrentUserProvider currentUserProvider;
  private final ActivityService activityService;
  private final ApplicationEventPublisher eventPublisher;

  // ── Create ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
    User currentUser = currentUserProvider.getCurrentUser();
    Project project = findProjectOrThrow(projectId);
    authorizeProjectOwner(project, currentUser);

    Task task = taskMapper.toEntity(request);
    task.setProject(project);
    task.setCreatedBy(currentUser);

    if (request.assigneeId() != null) {
      User assignee = findUserOrThrow(request.assigneeId());
      task.setAssignee(assignee);
    }

    Task saved = taskRepository.save(task);
    log.info(
        "Task created: id={}, title={}, project={}, by={}",
        saved.getId(),
        saved.getTitle(),
        projectId,
        currentUser.getId());

    activityService.record(
        currentUser,
        ActivityType.TASK_CREATED,
        "Task created",
        projectId,
        null,
        saved.getId(),
        null);
        
    eventPublisher.publishEvent(new TaskCreatedEvent(this, saved.getId(), projectId));
    
    return taskMapper.toResponse(saved);
  }

  // ── Update ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
    User currentUser = currentUserProvider.getCurrentUser();
    Task task = findTaskOrThrow(taskId);
    authorizeProjectOwner(task.getProject(), currentUser);

    taskMapper.updateEntityFromRequest(request, task);
    Task saved = taskRepository.save(task);

    log.info("Task updated: id={}", taskId);
    activityService.record(
        currentUser,
        ActivityType.TASK_UPDATED,
        "Task updated",
        task.getProject().getId(),
        null,
        saved.getId(),
        null);
        
    eventPublisher.publishEvent(new TaskUpdatedEvent(this, saved.getId(), task.getProject().getId()));
    
    return taskMapper.toResponse(saved);
  }

  // ── Delete ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public void deleteTask(UUID taskId) {
    User currentUser = currentUserProvider.getCurrentUser();
    Task task = findTaskOrThrow(taskId);
    authorizeProjectOwner(task.getProject(), currentUser);

    taskRepository.delete(task);
    log.info("Task deleted: id={}, by={}", taskId, currentUser.getId());
    activityService.record(
        currentUser,
        ActivityType.TASK_DELETED,
        "Task deleted",
        task.getProject().getId(),
        null,
        taskId,
        null);
  }

  // ── Read ─────────────────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public TaskResponse getTask(UUID taskId) {
    User currentUser = currentUserProvider.getCurrentUser();
    Task task = findTaskOrThrow(taskId);
    authorizeTaskAccess(task, currentUser);

    return taskMapper.toResponse(task);
  }

  // ── Status ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public TaskResponse changeStatus(UUID taskId, UpdateTaskStatusRequest request) {
    User currentUser = currentUserProvider.getCurrentUser();
    Task task = findTaskOrThrow(taskId);

    // Assignee OR project owner may update status
    boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
    boolean isAssignee =
        task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());

    if (!isOwner && !isAssignee) {
      log.warn("Unauthorized status change attempt: user={}, task={}", currentUser.getId(), taskId);
      throw new AccessDeniedException(
          "Only the project owner or task assignee may update the task status.");
    }

    task.setStatus(request.status());
    Task saved = taskRepository.save(task);

    if (request.status().name().equals("DONE")) {
      log.info("Task completed: id={}, by={}", taskId, currentUser.getId());
      activityService.record(
          currentUser,
          ActivityType.TASK_COMPLETED,
          "Task completed",
          task.getProject().getId(),
          null,
          saved.getId(),
          null);
    } else {
      log.info(
          "Task status changed: id={}, status={}, by={}",
          taskId,
          request.status(),
          currentUser.getId());
      activityService.record(
          currentUser,
          ActivityType.TASK_STATUS_CHANGED,
          "Task status changed",
          task.getProject().getId(),
          null,
          saved.getId(),
          java.util.Map.of("status", request.status().name()));
    }
    
    eventPublisher.publishEvent(new TaskUpdatedEvent(this, saved.getId(), task.getProject().getId()));
    
    return taskMapper.toResponse(saved);
  }

  // ── Priority ─────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public TaskResponse changePriority(UUID taskId, UpdateTaskPriorityRequest request) {
    User currentUser = currentUserProvider.getCurrentUser();
    Task task = findTaskOrThrow(taskId);
    authorizeProjectOwner(task.getProject(), currentUser);

    task.setPriority(request.priority());
    Task saved = taskRepository.save(task);

    log.info(
        "Task priority changed: id={}, priority={}, by={}",
        taskId,
        request.priority(),
        currentUser.getId());
    activityService.record(
        currentUser,
        ActivityType.TASK_UPDATED,
        "Task priority changed",
        task.getProject().getId(),
        null,
        saved.getId(),
        java.util.Map.of("priority", request.priority().name()));
        
    eventPublisher.publishEvent(new TaskUpdatedEvent(this, saved.getId(), task.getProject().getId()));
    
    return taskMapper.toResponse(saved);
  }

  // ── Assign ───────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public TaskResponse assignTask(UUID taskId, UpdateTaskAssigneeRequest request) {
    User currentUser = currentUserProvider.getCurrentUser();
    Task task = findTaskOrThrow(taskId);
    authorizeProjectOwner(task.getProject(), currentUser);

    if (request.assigneeId() == null) {
      task.setAssignee(null);
      log.info("Task unassigned: id={}, by={}", taskId, currentUser.getId());
    } else {
      User assignee = findUserOrThrow(request.assigneeId());
      task.setAssignee(assignee);
      log.info(
          "Task assigned: id={}, assignee={}, by={}",
          taskId,
          assignee.getId(),
          currentUser.getId());
    }

    Task saved = taskRepository.save(task);
    activityService.record(
        currentUser,
        ActivityType.TASK_ASSIGNED,
        "Task assignment updated",
        task.getProject().getId(),
        null,
        saved.getId(),
        java.util.Map.of(
            "assigneeId", request.assigneeId() == null ? "unassigned" : request.assigneeId()));
            
    eventPublisher.publishEvent(new TaskUpdatedEvent(this, saved.getId(), task.getProject().getId()));
    
    return taskMapper.toResponse(saved);
  }

  // ── List (project) ────────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Page<TaskResponse> listProjectTasks(UUID projectId, Pageable pageable) {
    User currentUser = currentUserProvider.getCurrentUser();
    Project project = findProjectOrThrow(projectId);
    authorizeProjectOwner(project, currentUser);

    return taskRepository.findByProjectId(projectId, pageable).map(taskMapper::toResponse);
  }

  // ── List (mine) ───────────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Page<TaskResponse> listMyTasks(Pageable pageable) {
    User currentUser = currentUserProvider.getCurrentUser();
    return taskRepository
        .findByAssigneeId(currentUser.getId(), pageable)
        .map(taskMapper::toResponse);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private Task findTaskOrThrow(UUID taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(
            () -> {
              log.warn("Task not found: id={}", taskId);
              return new ResourceNotFoundException(
                  "TASK_NOT_FOUND", "Task not found with id: " + taskId);
            });
  }

  private Project findProjectOrThrow(UUID projectId) {
    return projectRepository
        .findById(projectId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "PROJECT_NOT_FOUND", "Project not found with id: " + projectId));
  }

  private User findUserOrThrow(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "USER_NOT_FOUND", "User not found with id: " + userId));
  }

  /**
   * Verifies the current user is the project owner. All destructive operations (create, update,
   * delete, assign, priority) require this.
   */
  private void authorizeProjectOwner(Project project, User currentUser) {
    if (!project.getOwner().getId().equals(currentUser.getId())) {
      log.warn(
          "Unauthorized access: user={} attempted action on project={} owned by={}",
          currentUser.getId(),
          project.getId(),
          project.getOwner().getId());
      throw new AccessDeniedException(
          "You do not have permission to perform this action on this project.");
    }
  }

  /** Allows the project owner OR the task assignee to view a task. */
  private void authorizeTaskAccess(Task task, User currentUser) {
    boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
    boolean isAssignee =
        task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());

    if (!isOwner && !isAssignee) {
      log.warn("Unauthorized read attempt: user={} on task={}", currentUser.getId(), task.getId());
      throw new AccessDeniedException("You do not have permission to view this task.");
    }
  }
}

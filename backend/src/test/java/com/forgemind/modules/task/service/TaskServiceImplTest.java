package com.forgemind.modules.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.activity.entity.ActivityType;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.repository.UserRepository;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import org.springframework.context.ApplicationEventPublisher;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.repository.ProjectRepository;
import com.forgemind.modules.task.dto.request.CreateTaskRequest;
import com.forgemind.modules.task.dto.response.TaskResponse;
import com.forgemind.modules.task.entity.Task;
import com.forgemind.modules.task.entity.TaskPriority;
import com.forgemind.modules.task.entity.TaskStatus;
import com.forgemind.modules.task.mapper.TaskMapper;
import com.forgemind.modules.task.repository.TaskRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

  @Mock private TaskRepository taskRepository;

  @Mock private ProjectRepository projectRepository;

  @Mock private UserRepository userRepository;

  @Mock private TaskMapper taskMapper;

  @Mock private CurrentUserProvider currentUserProvider;

  @Mock private ActivityService activityService;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private TaskServiceImpl taskService;

  private User currentUser;
  private Project project;
  private Task task;
  private UUID projectId;
  private UUID taskId;
  private CreateTaskRequest createRequest;
  private TaskResponse response;

  @BeforeEach
  void setUp() {
    projectId = UUID.randomUUID();
    taskId = UUID.randomUUID();

    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setUsername("testuser");
    currentUser.setFirstName("John");
    currentUser.setLastName("Doe");

    project = new Project();
    project.setId(projectId);
    project.setName("Test Project");
    project.setOwner(currentUser);

    task = new Task();
    task.setId(taskId);
    task.setTitle("Test Task");
    task.setProject(project);
    task.setCreatedBy(currentUser);

    createRequest = new CreateTaskRequest("Test Task", "Desc", TaskPriority.MEDIUM, null, null);

    TaskResponse.AssigneeInfo assigneeInfo =
        new TaskResponse.AssigneeInfo(1L, "testuser", "John", "Doe");
    response =
        new TaskResponse(
            taskId,
            projectId,
            "Test Project",
            "Test Task",
            "Desc",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            null,
            assigneeInfo,
            null,
            Instant.now(),
            Instant.now());
  }

  @Test
  @DisplayName("createTask - Success")
  void createTask_Success() {
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(taskMapper.toEntity(createRequest)).thenReturn(task);
    when(taskRepository.save(any(Task.class))).thenReturn(task);
    when(taskMapper.toResponse(task)).thenReturn(response);

    TaskResponse result = taskService.createTask(projectId, createRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(taskId);
    verify(taskRepository).save(task);
    verify(activityService)
        .record(
            eq(currentUser),
            eq(ActivityType.TASK_CREATED),
            any(),
            eq(projectId),
            any(),
            eq(taskId),
            any());
  }

  @Test
  @DisplayName("createTask - Access Denied (Not Owner)")
  void createTask_AccessDenied() {
    User otherUser = new User();
    otherUser.setId(2L);
    project.setOwner(otherUser);

    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    assertThatThrownBy(() -> taskService.createTask(projectId, createRequest))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @DisplayName("getTask - Success (owner accesses task)")
  void getTask_Success() {
    // currentUser is the project owner — should be allowed
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskMapper.toResponse(task)).thenReturn(response);

    TaskResponse result = taskService.getTask(taskId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(taskId);
  }

  @Test
  @DisplayName("deleteTask - Success")
  void deleteTask_Success() {
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    taskService.deleteTask(taskId);

    verify(taskRepository).delete(task);
    verify(activityService)
        .record(
            eq(currentUser),
            eq(ActivityType.TASK_DELETED),
            any(),
            eq(projectId),
            any(),
            eq(taskId),
            any());
  }

  @Test
  @DisplayName("deleteTask - Resource Not Found")
  void deleteTask_NotFound() {
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.deleteTask(taskId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}

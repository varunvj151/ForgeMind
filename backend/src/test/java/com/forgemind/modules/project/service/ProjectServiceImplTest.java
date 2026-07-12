package com.forgemind.modules.project.service;

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
import com.forgemind.modules.auth.security.CurrentUserProvider;
import org.springframework.context.ApplicationEventPublisher;
import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.project.entity.ProjectStatus;
import com.forgemind.modules.project.mapper.ProjectMapper;
import com.forgemind.modules.project.repository.ProjectRepository;
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
class ProjectServiceImplTest {

  @Mock private ProjectRepository projectRepository;

  @Mock private ProjectMapper projectMapper;

  @Mock private CurrentUserProvider currentUserProvider;

  @Mock private ActivityService activityService;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ProjectServiceImpl projectService;

  private User currentUser;
  private Project project;
  private ProjectRequest request;
  private ProjectResponse response;
  private UUID projectId;

  @BeforeEach
  void setUp() {
    projectId = UUID.randomUUID();

    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setUsername("testuser");
    currentUser.setEmail("user@example.com");

    project = new Project();
    project.setId(projectId);
    project.setName("Test Project");
    project.setOwner(currentUser);

    request = new ProjectRequest("Test Project", "Desc", ProjectStatus.ACTIVE);

    response =
        new ProjectResponse(
            projectId,
            "Test Project",
            "Desc",
            ProjectStatus.ACTIVE,
            Instant.now(),
            Instant.now(),
            1L,
            "testuser");
  }

  @Test
  @DisplayName("createProject - Success")
  void createProject_Success() {
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(projectMapper.toEntity(request)).thenReturn(project);
    when(projectRepository.save(any(Project.class))).thenReturn(project);
    when(projectMapper.toResponse(project)).thenReturn(response);

    ProjectResponse result = projectService.createProject(request);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(projectId);

    verify(projectRepository).save(project);
    verify(activityService)
        .record(
            eq(currentUser),
            eq(ActivityType.PROJECT_CREATED),
            any(),
            eq(projectId),
            any(),
            any(),
            any());
  }

  @Test
  @DisplayName("getProjectById - Success")
  void getProjectById_Success() {
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectMapper.toResponse(project)).thenReturn(response);

    ProjectResponse result = projectService.getProjectById(projectId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(projectId);
  }

  @Test
  @DisplayName("getProjectById - Not Found")
  void getProjectById_NotFound() {
    when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectService.getProjectById(projectId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("updateProject - Success")
  void updateProject_Success() {
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
    when(projectRepository.save(project)).thenReturn(project);
    when(projectMapper.toResponse(project)).thenReturn(response);

    ProjectResponse result = projectService.updateProject(projectId, request);

    assertThat(result).isNotNull();
    verify(projectMapper).updateEntityFromRequest(request, project);
    verify(projectRepository).save(project);
  }

  @Test
  @DisplayName("updateProject - Access Denied")
  void updateProject_AccessDenied() {
    User otherUser = new User();
    otherUser.setId(2L);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(currentUserProvider.getCurrentUser()).thenReturn(otherUser); // different user

    assertThatThrownBy(() -> projectService.updateProject(projectId, request))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  @DisplayName("deleteProject - Success")
  void deleteProject_Success() {
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

    projectService.deleteProject(projectId);

    verify(projectRepository).delete(project);
    verify(activityService)
        .record(
            eq(currentUser),
            eq(ActivityType.PROJECT_DELETED),
            any(),
            eq(projectId),
            any(),
            any(),
            any());
  }
}

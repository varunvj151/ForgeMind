package com.forgemind.modules.project.service;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.project.entity.Project;
import com.forgemind.modules.activity.entity.ActivityType;
import com.forgemind.modules.activity.service.ActivityService;
import com.forgemind.modules.project.mapper.ProjectMapper;
import com.forgemind.modules.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

/**
 * Default implementation of {@link ProjectService}.
 *
 * <p>This class is the only place that contains business logic for the Project module.
 * It never exposes JPA entities outside its boundary — all return values are DTOs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper     projectMapper;
    private final CurrentUserProvider currentUserProvider;
    private final ActivityService activityService;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Project project = projectMapper.toEntity(request);
        project.setOwner(currentUser);
        Project saved   = projectRepository.save(project);

        log.info("Project created: id={}, name={}", saved.getId(), saved.getName());
        activityService.record(currentUser, ActivityType.PROJECT_CREATED, "Project created", saved.getId(), null, null, null);
        return projectMapper.toResponse(saved);
    }

    // ── Read (all) ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(projectMapper::toResponse);
    }

    // ── Read (single) ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        Project project = findOrThrow(id);
        return projectMapper.toResponse(project);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        Project project = findOrThrow(id);
        authorizeOwner(project);
        
        projectMapper.updateEntityFromRequest(request, project);
        Project saved = projectRepository.save(project);

        log.info("Project updated: id={}, name={}", saved.getId(), saved.getName());
        activityService.record(currentUserProvider.getCurrentUser(), ActivityType.PROJECT_UPDATED, "Project updated", saved.getId(), null, null, null);
        return projectMapper.toResponse(saved);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        Project project = findOrThrow(id);
        authorizeOwner(project);
        
        projectRepository.delete(project);

        log.info("Project deleted: id={}", id);
        activityService.record(currentUserProvider.getCurrentUser(), ActivityType.PROJECT_DELETED, "Project deleted", id, null, null, null);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Fetches a Project by ID or throws a {@link ResourceNotFoundException}.
     * Centralises the not-found logic so it is never duplicated across methods.
     */
    private Project findOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found: id={}", id);
                    return new ResourceNotFoundException("PROJECT_NOT_FOUND",
                            "Project not found with id: " + id);
                });
    }
    
    /**
     * Checks if the current authenticated user is the owner of the project.
     * Throws an AccessDeniedException if they are not.
     */
    private void authorizeOwner(Project project) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            log.warn("Access denied: User {} attempted to modify Project {} owned by User {}", 
                    currentUser.getId(), project.getId(), project.getOwner().getId());
            throw new AccessDeniedException("You do not have permission to modify this project.");
        }
    }
}

package com.forgemind.modules.ai.agent.security;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Central authorization checkpoint for the agent subsystem.
 *
 * <p>Agents run on behalf of the currently authenticated user and must never surface data from a
 * project that user cannot access. Rather than trusting each tool to remember this, every agent
 * asserts access <em>once, up front</em> through this guard before gathering any context or calling
 * the provider. The guard reuses the existing {@link CurrentUserProvider} and project ownership
 * model — it does not introduce a parallel security scheme.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentAccessGuard {

  private final CurrentUserProvider currentUserProvider;
  private final ProjectService projectService;

  /**
   * Verifies the current user is allowed to run an agent against the given project.
   *
   * @param projectId the project the agent will operate on
   * @return the resolved, access-checked project (handy so callers avoid a second lookup)
   * @throws AccessDeniedException if the current user does not own the project
   */
  public ProjectResponse requireProjectAccess(UUID projectId) {
    User currentUser = currentUserProvider.getCurrentUser();
    ProjectResponse project = projectService.getProjectById(projectId);

    if (project.ownerId() == null || !project.ownerId().equals(currentUser.getId())) {
      log.warn(
          "Agent access denied: user {} attempted to run an agent on project {} owned by {}",
          currentUser.getId(),
          projectId,
          project.ownerId());
      throw new AccessDeniedException(
          "You do not have permission to run AI agents on this project.");
    }
    return project;
  }
}

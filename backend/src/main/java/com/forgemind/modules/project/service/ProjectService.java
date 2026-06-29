package com.forgemind.modules.project.service;

import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for Project management.
 *
 * <p>All methods accept and return DTOs — entities never cross this boundary.
 */
public interface ProjectService {

  /**
   * Creates a new project.
   *
   * @param request validated project data
   * @return the created project as a response DTO
   */
  ProjectResponse createProject(ProjectRequest request);

  /**
   * Returns a paginated list of all projects.
   *
   * @param pageable pagination and sorting parameters
   * @return page of project response DTOs
   */
  Page<ProjectResponse> getAllProjects(Pageable pageable);

  /**
   * Returns a single project by ID.
   *
   * @param id project UUID
   * @return the project as a response DTO
   * @throws com.forgemind.common.exception.ResourceNotFoundException if not found
   */
  ProjectResponse getProjectById(UUID id);

  /**
   * Updates an existing project.
   *
   * @param id project UUID
   * @param request validated updated data
   * @return the updated project as a response DTO
   * @throws com.forgemind.common.exception.ResourceNotFoundException if not found
   */
  ProjectResponse updateProject(UUID id, ProjectRequest request);

  /**
   * Deletes a project by ID.
   *
   * @param id project UUID
   * @throws com.forgemind.common.exception.ResourceNotFoundException if not found
   */
  void deleteProject(UUID id);
}

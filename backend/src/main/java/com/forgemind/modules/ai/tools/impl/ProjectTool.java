package com.forgemind.modules.ai.tools.impl;

import com.forgemind.modules.ai.tools.AITool;
import com.forgemind.modules.ai.tools.ToolNames;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fetches a single project's metadata. Thin wrapper over {@link ProjectService} so agents never
 * touch repositories directly and always go through the authenticated domain service.
 */
@Component
@RequiredArgsConstructor
public class ProjectTool implements AITool<UUID, ProjectResponse> {

  private final ProjectService projectService;

  @Override
  public String name() {
    return ToolNames.PROJECT;
  }

  @Override
  public String description() {
    return "Fetches a project's core metadata (name, description, status, owner) by id.";
  }

  @Override
  public ProjectResponse execute(UUID projectId) {
    return projectService.getProjectById(projectId);
  }
}

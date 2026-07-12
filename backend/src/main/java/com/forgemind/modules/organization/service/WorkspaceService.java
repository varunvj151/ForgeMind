package com.forgemind.modules.organization.service;

import com.forgemind.modules.organization.dto.WorkspaceDto.CreateWorkspaceRequest;
import com.forgemind.modules.organization.dto.WorkspaceDto.WorkspaceResponse;
import java.util.List;
import java.util.UUID;

/** Manages workspaces within an organization. */
public interface WorkspaceService {
  WorkspaceResponse createWorkspace(UUID organizationId, CreateWorkspaceRequest request);
  WorkspaceResponse getWorkspaceById(UUID organizationId, UUID workspaceId);
  List<WorkspaceResponse> listWorkspaces(UUID organizationId);
  void deleteWorkspace(UUID organizationId, UUID workspaceId);
}

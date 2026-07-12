package com.forgemind.modules.organization.service;

import com.forgemind.modules.organization.dto.WorkspaceDto.CreateWorkspaceRequest;
import com.forgemind.modules.organization.dto.WorkspaceDto.WorkspaceResponse;
import com.forgemind.modules.organization.entity.Organization;
import com.forgemind.modules.organization.entity.Workspace;
import com.forgemind.modules.organization.repository.OrganizationRepository;
import com.forgemind.modules.organization.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

  private final WorkspaceRepository workspaceRepository;
  private final OrganizationRepository organizationRepository;

  @Override
  @Transactional
  public WorkspaceResponse createWorkspace(UUID organizationId, CreateWorkspaceRequest request) {
    Organization org = organizationRepository.findById(organizationId)
        .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));
    if (workspaceRepository.existsByOrganizationIdAndSlug(organizationId, request.slug())) {
      throw new IllegalArgumentException("Workspace slug already exists in this organization: " + request.slug());
    }
    Workspace ws = Workspace.builder()
        .organization(org)
        .name(request.name())
        .slug(request.slug())
        .build();
    ws = workspaceRepository.save(ws);
    log.info("Workspace created: id={}, slug={}, org={}", ws.getId(), ws.getSlug(), organizationId);
    return toResponse(ws);
  }

  @Override
  @Transactional(readOnly = true)
  public WorkspaceResponse getWorkspaceById(UUID organizationId, UUID workspaceId) {
    Workspace ws = workspaceRepository.findById(workspaceId)
        .filter(w -> w.getOrganization().getId().equals(organizationId))
        .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
    return toResponse(ws);
  }

  @Override
  @Transactional(readOnly = true)
  public List<WorkspaceResponse> listWorkspaces(UUID organizationId) {
    return workspaceRepository.findAllByOrganizationId(organizationId).stream()
        .map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public void deleteWorkspace(UUID organizationId, UUID workspaceId) {
    Workspace ws = workspaceRepository.findById(workspaceId)
        .filter(w -> w.getOrganization().getId().equals(organizationId))
        .orElseThrow(() -> new EntityNotFoundException("Workspace not found: " + workspaceId));
    workspaceRepository.delete(ws);
    log.info("Workspace deleted: id={}", workspaceId);
  }

  private WorkspaceResponse toResponse(Workspace ws) {
    return new WorkspaceResponse(ws.getId(), ws.getOrganization().getId(),
        ws.getName(), ws.getSlug(), ws.getCreatedAt());
  }
}

package com.forgemind.modules.organization.controller;

import com.forgemind.modules.organization.dto.WorkspaceDto.CreateWorkspaceRequest;
import com.forgemind.modules.organization.dto.WorkspaceDto.WorkspaceResponse;
import com.forgemind.modules.organization.service.WorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

  private final WorkspaceService workspaceService;

  @PostMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<WorkspaceResponse> createWorkspace(
      @PathVariable UUID orgId,
      @Valid @RequestBody CreateWorkspaceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(workspaceService.createWorkspace(orgId, request));
  }

  @GetMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'VIEWER')")
  public ResponseEntity<List<WorkspaceResponse>> listWorkspaces(@PathVariable UUID orgId) {
    return ResponseEntity.ok(workspaceService.listWorkspaces(orgId));
  }

  @GetMapping("/{workspaceId}")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'VIEWER')")
  public ResponseEntity<WorkspaceResponse> getWorkspace(
      @PathVariable UUID orgId, @PathVariable UUID workspaceId) {
    return ResponseEntity.ok(workspaceService.getWorkspaceById(orgId, workspaceId));
  }

  @DeleteMapping("/{workspaceId}")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<Void> deleteWorkspace(
      @PathVariable UUID orgId, @PathVariable UUID workspaceId) {
    workspaceService.deleteWorkspace(orgId, workspaceId);
    return ResponseEntity.noContent().build();
  }
}

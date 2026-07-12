package com.forgemind.modules.api.v2;

import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/organizations/{orgId}/projects")
@RequiredArgsConstructor
@Tag(name = "Projects (v2)", description = "Public Developer API for managing Projects")
public class V2ProjectController {

  private final ProjectService projectService;

  @GetMapping
  @Operation(summary = "List projects", description = "Returns a paginated list of projects in the organization.")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'VIEWER')")
  public ResponseEntity<List<ProjectResponse>> listProjects(@PathVariable UUID orgId) {
    // In a full implementation, this would accept pagination parameters (cursor, limit).
    // Reusing the existing v1 service for this demonstration.
    return ResponseEntity.ok(projectService.getAllProjects(Pageable.unpaged()).getContent());
  }
}

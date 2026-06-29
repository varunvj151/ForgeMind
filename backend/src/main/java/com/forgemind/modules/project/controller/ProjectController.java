package com.forgemind.modules.project.controller;

import com.forgemind.common.exception.ErrorResponse;
import com.forgemind.modules.project.dto.request.ProjectRequest;
import com.forgemind.modules.project.dto.response.ProjectResponse;
import com.forgemind.modules.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller for Project management.
 *
 * <p>This controller contains no business logic. All operations are delegated directly to {@link
 * ProjectService}. It is responsible only for:
 *
 * <ul>
 *   <li>Accepting and validating request bodies
 *   <li>Delegating to the service layer
 *   <li>Returning the correct HTTP status codes
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

  private final ProjectService projectService;

  // ── POST /api/projects ────────────────────────────────────────────────────

  @Operation(
      summary = "Create a project",
      description = "Creates a new project and returns it with its assigned ID and timestamps.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Project created"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorised — missing or invalid JWT",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {

    ProjectResponse created = projectService.createProject(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

    return ResponseEntity.created(location).body(created);
  }

  // ── GET /api/projects ─────────────────────────────────────────────────────

  @Operation(
      summary = "List all projects",
      description =
          "Returns a paginated, sorted list of all projects. "
              + "Use ?page=0&size=20&sort=name,asc to customise.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List returned"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  public ResponseEntity<Page<ProjectResponse>> getAllProjects(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    return ResponseEntity.ok(projectService.getAllProjects(pageable));
  }

  // ── GET /api/projects/{id} ────────────────────────────────────────────────

  @Operation(
      summary = "Get project by ID",
      description = "Returns a single project identified by its UUID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Project found"),
    @ApiResponse(
        responseCode = "404",
        description = "Project not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<ProjectResponse> getProjectById(
      @Parameter(description = "Project UUID") @PathVariable UUID id) {

    return ResponseEntity.ok(projectService.getProjectById(id));
  }

  // ── PUT /api/projects/{id} ────────────────────────────────────────────────

  @Operation(
      summary = "Update a project",
      description = "Replaces an existing project's data. ID and timestamps remain unchanged.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Project updated"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Project not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/{id}")
  public ResponseEntity<ProjectResponse> updateProject(
      @Parameter(description = "Project UUID") @PathVariable UUID id,
      @Valid @RequestBody ProjectRequest request) {

    return ResponseEntity.ok(projectService.updateProject(id, request));
  }

  // ── DELETE /api/projects/{id} ─────────────────────────────────────────────

  @Operation(
      summary = "Delete a project",
      description = "Permanently deletes a project. This action cannot be undone.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Project deleted"),
    @ApiResponse(
        responseCode = "404",
        description = "Project not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorised",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProject(
      @Parameter(description = "Project UUID") @PathVariable UUID id) {

    projectService.deleteProject(id);
    return ResponseEntity.noContent().build();
  }
}

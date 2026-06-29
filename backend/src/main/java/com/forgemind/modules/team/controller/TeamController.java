package com.forgemind.modules.team.controller;

import com.forgemind.common.exception.ErrorResponse;
import com.forgemind.modules.team.dto.request.AddTeamMemberRequest;
import com.forgemind.modules.team.dto.request.TeamRequest;
import com.forgemind.modules.team.dto.response.TeamMemberResponse;
import com.forgemind.modules.team.dto.response.TeamResponse;
import com.forgemind.modules.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management and collaboration endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

  private final TeamService teamService;

  // ── Team CRUD ────────────────────────────────────────────────────────────

  @Operation(summary = "Create a new team")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Team created"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
    TeamResponse response = teamService.createTeam(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @Operation(summary = "List all teams for the current user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of teams"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  public ResponseEntity<Page<TeamResponse>> listTeams(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(teamService.listTeams(pageable));
  }

  @Operation(summary = "Get team by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Team details"),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied (not a member)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID id) {
    return ResponseEntity.ok(teamService.getTeamById(id));
  }

  @Operation(summary = "Update team details")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Team updated"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied (requires ADMIN or OWNER role)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/{id}")
  public ResponseEntity<TeamResponse> updateTeam(
      @PathVariable UUID id, @Valid @RequestBody TeamRequest request) {
    return ResponseEntity.ok(teamService.updateTeam(id, request));
  }

  @Operation(summary = "Delete a team")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Team deleted"),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied (requires OWNER role)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTeam(@PathVariable UUID id) {
    teamService.deleteTeam(id);
    return ResponseEntity.noContent().build();
  }

  // ── Team Members ─────────────────────────────────────────────────────────

  @Operation(summary = "List team members")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "List of members"),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied (not a member)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{id}/members")
  public ResponseEntity<Page<TeamMemberResponse>> listMembers(
      @PathVariable UUID id, @PageableDefault(size = 50, sort = "joinedAt") Pageable pageable) {
    return ResponseEntity.ok(teamService.listMembers(id, pageable));
  }

  @Operation(summary = "Add a member to the team")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Member added"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied (requires ADMIN or OWNER role)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Team or User not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "User is already a member",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/{id}/members")
  public ResponseEntity<TeamMemberResponse> addMember(
      @PathVariable UUID id, @Valid @RequestBody AddTeamMemberRequest request) {
    TeamMemberResponse response = teamService.addMember(id, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Remove a member from the team")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Member removed"),
    @ApiResponse(
        responseCode = "400",
        description = "Cannot remove an owner",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Access denied (requires ADMIN or OWNER role)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Team or Member not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/{id}/members/{userId}")
  public ResponseEntity<Void> removeMember(@PathVariable UUID id, @PathVariable Long userId) {
    teamService.removeMember(id, userId);
    return ResponseEntity.noContent().build();
  }
}

package com.forgemind.modules.activity.controller;

import com.forgemind.common.exception.ErrorResponse;
import com.forgemind.modules.activity.dto.response.ActivityResponse;
import com.forgemind.modules.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Activity Feed", description = "Read-only activity timeline endpoints. Activities are system-generated.")
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Get activity feed for a project (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project activity timeline"),
            @ApiResponse(responseCode = "403", description = "Not the project owner", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/projects/{id}/activities")
    public ResponseEntity<Page<ActivityResponse>> getProjectActivities(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(activityService.getProjectActivities(id, pageable));
    }

    @Operation(summary = "Get activity feed for a team (members only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team activity timeline"),
            @ApiResponse(responseCode = "403", description = "Not a team member", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/teams/{id}/activities")
    public ResponseEntity<Page<ActivityResponse>> getTeamActivities(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(activityService.getTeamActivities(id, pageable));
    }

    @Operation(summary = "Get my personal activity history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "My activity history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users/me/activities")
    public ResponseEntity<Page<ActivityResponse>> getMyActivities(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(activityService.getMyActivities(pageable));
    }
}

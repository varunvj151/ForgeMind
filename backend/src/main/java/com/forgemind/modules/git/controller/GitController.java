package com.forgemind.modules.git.controller;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.git.dto.request.ConnectRepositoryRequest;
import com.forgemind.modules.git.dto.request.TriggerSyncRequest;
import com.forgemind.modules.git.dto.response.GitRepositoryResponse;
import com.forgemind.modules.git.service.GitRepositoryService;
import com.forgemind.modules.git.service.GitSyncService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/git/repositories")
public class GitController {

  private final GitRepositoryService gitRepositoryService;
  private final GitSyncService gitSyncService;

  public GitController(GitRepositoryService gitRepositoryService, GitSyncService gitSyncService) {
    this.gitRepositoryService = gitRepositoryService;
    this.gitSyncService = gitSyncService;
  }

  @PostMapping
  public ResponseEntity<GitRepositoryResponse> connectRepository(
      @Valid @RequestBody ConnectRepositoryRequest request,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(gitRepositoryService.connectRepository(request, user.getId()));
  }

  @GetMapping
  public ResponseEntity<List<GitRepositoryResponse>> listRepositories(
      @RequestParam UUID projectId,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(gitRepositoryService.listProjectRepositories(projectId, user.getId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<GitRepositoryResponse> getRepository(
      @PathVariable UUID id,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(gitRepositoryService.getRepository(id, user.getId()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> disconnectRepository(
      @PathVariable UUID id,
      @AuthenticationPrincipal User user) {
    gitRepositoryService.disconnectRepository(id, user.getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/sync")
  public ResponseEntity<Void> triggerSync(
      @Valid @RequestBody TriggerSyncRequest request,
      @AuthenticationPrincipal User user) {
    // Basic auth check would go here in Phase 2
    gitSyncService.syncRepositoryAsync(request.getRepositoryId());
    return ResponseEntity.accepted().build();
  }
}

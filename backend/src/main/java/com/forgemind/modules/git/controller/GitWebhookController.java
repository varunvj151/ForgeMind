package com.forgemind.modules.git.controller;

import com.forgemind.modules.git.webhook.GitHubWebhookHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/github")
public class GitWebhookController {

  private final GitHubWebhookHandler githubWebhookHandler;

  public GitWebhookController(GitHubWebhookHandler githubWebhookHandler) {
    this.githubWebhookHandler = githubWebhookHandler;
  }

  @PostMapping
  public ResponseEntity<Void> handleGithubWebhook(
      @RequestHeader("X-GitHub-Event") String event,
      @RequestHeader("X-Hub-Signature-256") String signature,
      @RequestBody String payload) {
      
    githubWebhookHandler.handle(event, signature, payload);
    return ResponseEntity.ok().build();
  }
}

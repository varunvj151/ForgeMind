package com.forgemind.modules.git.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.git.config.GitMetrics;
import com.forgemind.modules.git.config.GitProperties;
import com.forgemind.modules.git.entity.GitRepository;
import com.forgemind.modules.git.provider.GitProviderType;
import com.forgemind.modules.git.repository.GitRepositoryRepository;
import com.forgemind.modules.git.service.GitSyncService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GitHubWebhookHandler {

  private final GitRepositoryRepository gitRepositoryRepository;
  private final GitSyncService gitSyncService;
  private final GitProperties gitProperties;
  private final GitMetrics gitMetrics;
  private final ObjectMapper objectMapper;

  public GitHubWebhookHandler(
      GitRepositoryRepository gitRepositoryRepository,
      GitSyncService gitSyncService,
      GitProperties gitProperties,
      GitMetrics gitMetrics,
      ObjectMapper objectMapper) {
    this.gitRepositoryRepository = gitRepositoryRepository;
    this.gitSyncService = gitSyncService;
    this.gitProperties = gitProperties;
    this.gitMetrics = gitMetrics;
    this.objectMapper = objectMapper;
  }

  public void handle(String event, String signature, String payload) {
    gitMetrics.recordWebhookEvent("GITHUB", event);

    try {
      JsonNode node = objectMapper.readTree(payload);
      String fullName = node.path("repository").path("full_name").asText();

      if (fullName == null || fullName.isEmpty()) {
        log.warn("GitHub webhook missing repository full_name");
        return;
      }

      String[] parts = fullName.split("/");
      if (parts.length != 2) return;

      GitRepository repo = gitRepositoryRepository.findByProviderAndOwnerAndRepoName(
          GitProviderType.GITHUB.name(), parts[0], parts[1]).orElse(null);

      if (repo == null || !repo.isWebhookActive()) {
        return;
      }

      if (gitProperties.isWebhookSignatureRequired()) {
          verifySignature(repo.getWebhookSecret(), payload, signature);
      }

      // We trigger a full asynchronous sync on supported events
      if ("push".equals(event) || "pull_request".equals(event) || "release".equals(event)) {
          gitSyncService.syncRepositoryAsync(repo.getId());
      }

    } catch (Exception e) {
      log.error("Failed to process GitHub webhook", e);
      throw new RuntimeException("Webhook processing failed", e);
    }
  }

  private void verifySignature(String secret, String payload, String signatureHeader) {
      if (secret == null || secret.isEmpty()) {
          throw new SecurityException("Webhook secret not configured for repository");
      }
      if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
          throw new SecurityException("Invalid webhook signature header");
      }

      String expectedSignature = "sha256=" + calculateHmac(secret, payload);
      if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), signatureHeader.getBytes(StandardCharsets.UTF_8))) {
          throw new SecurityException("Webhook signature verification failed");
      }
  }

  private String calculateHmac(String secret, String payload) {
      try {
          Mac mac = Mac.getInstance("HmacSHA256");
          SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
          mac.init(secretKeySpec);
          byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
          StringBuilder sb = new StringBuilder(hmacBytes.length * 2);
          for (byte b : hmacBytes) {
              sb.append(String.format("%02x", b));
          }
          return sb.toString();
      } catch (Exception e) {
          throw new RuntimeException("Failed to calculate HMAC", e);
      }
  }
}

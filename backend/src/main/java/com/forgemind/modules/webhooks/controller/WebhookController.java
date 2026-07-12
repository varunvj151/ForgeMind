package com.forgemind.modules.webhooks.controller;

import com.forgemind.modules.webhooks.entity.WebhookEndpoint;
import com.forgemind.modules.webhooks.repository.WebhookEndpointRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/webhooks")
@RequiredArgsConstructor
public class WebhookController {

  private final WebhookEndpointRepository endpointRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public record CreateWebhookRequest(
      @NotBlank String url,
      @NotEmpty List<String> events
  ) {}

  public record WebhookResponse(
      UUID id, String url, List<String> events, boolean active, String secret
  ) {}

  @PostMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<WebhookResponse> createWebhook(
      @PathVariable UUID orgId,
      @Valid @RequestBody CreateWebhookRequest request) {
    
    byte[] secretBytes = new byte[32];
    secureRandom.nextBytes(secretBytes);
    String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);

    WebhookEndpoint endpoint = WebhookEndpoint.builder()
        .organizationId(orgId)
        .url(request.url())
        .events(request.events())
        .secret(secret)
        .build();

    endpoint = endpointRepository.save(endpoint);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new WebhookResponse(
            endpoint.getId(), endpoint.getUrl(), endpoint.getEvents(), 
            endpoint.isActive(), secret)); // Secret returned ONLY on creation
  }

  @GetMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<List<WebhookResponse>> listWebhooks(@PathVariable UUID orgId) {
    List<WebhookResponse> responses = endpointRepository.findAllByOrganizationId(orgId).stream()
        .map(e -> new WebhookResponse(e.getId(), e.getUrl(), e.getEvents(), e.isActive(), null))
        .toList();
    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("/{webhookId}")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<Void> deleteWebhook(
      @PathVariable UUID orgId, @PathVariable UUID webhookId) {
    endpointRepository.findById(webhookId)
        .filter(w -> w.getOrganizationId().equals(orgId))
        .ifPresent(endpointRepository::delete);
    return ResponseEntity.noContent().build();
  }
}

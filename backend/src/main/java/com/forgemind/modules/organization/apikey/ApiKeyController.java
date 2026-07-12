package com.forgemind.modules.organization.apikey;

import com.forgemind.modules.organization.apikey.ApiKeyService.ApiKeyCreationResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

  private final ApiKeyService apiKeyService;

  public record CreateApiKeyRequest(
      @NotBlank String name,
      @NotNull ApiKeyType type,
      List<String> scopes,
      Integer expiryDays) {}

  public record ApiKeyResponse(UUID id, String name, ApiKeyType type, List<String> scopes,
                               Instant expiresAt, Instant lastUsedAt, boolean active) {}

  @PostMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'MEMBER')")
  public ResponseEntity<ApiKeyCreationResult> createApiKey(
      @PathVariable UUID orgId,
      @RequestBody CreateApiKeyRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
        apiKeyService.generateApiKey(orgId, request.name(), request.type(),
            request.scopes(), request.expiryDays()));
  }

  @GetMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'MEMBER')")
  public ResponseEntity<List<ApiKeyResponse>> listApiKeys(@PathVariable UUID orgId) {
    List<ApiKeyResponse> keys = apiKeyService.listApiKeys(orgId).stream()
        .map(k -> new ApiKeyResponse(k.getId(), k.getName(), k.getType(),
            k.getScopes(), k.getExpiresAt(), k.getLastUsedAt(), k.isActive()))
        .toList();
    return ResponseEntity.ok(keys);
  }

  @DeleteMapping("/{keyId}")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'MEMBER')")
  public ResponseEntity<Void> revokeApiKey(
      @PathVariable UUID orgId, @PathVariable UUID keyId) {
    apiKeyService.revokeApiKey(orgId, keyId);
    return ResponseEntity.noContent().build();
  }
}

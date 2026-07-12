package com.forgemind.modules.organization.apikey;

import com.forgemind.modules.auth.entity.User;
import com.forgemind.modules.auth.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages API key generation, validation, and revocation. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

  private final ApiKeyRepository apiKeyRepository;
  private final CurrentUserProvider currentUserProvider;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Generates a new API key and returns the plaintext token ONCE.
   * Only the SHA-256 hash is persisted; the raw token is never stored.
   */
  @Transactional
  public ApiKeyCreationResult generateApiKey(
      UUID organizationId, String name, ApiKeyType type,
      List<String> scopes, Integer expiryDays) {

    User user = currentUserProvider.getCurrentUser();
    byte[] rawBytes = new byte[32];
    secureRandom.nextBytes(rawBytes);
    String rawToken = "fm_" + Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);
    String tokenHash = sha256Hex(rawToken);

    Instant expiresAt = expiryDays != null
        ? Instant.now().plus(expiryDays, ChronoUnit.DAYS)
        : null;

    ApiKey apiKey = ApiKey.builder()
        .organizationId(organizationId)
        .user(user)
        .name(name)
        .type(type)
        .tokenHash(tokenHash)
        .scopes(scopes)
        .expiresAt(expiresAt)
        .build();

    apiKey = apiKeyRepository.save(apiKey);
    log.info("API key generated: id={}, org={}, type={}", apiKey.getId(), organizationId, type);
    return new ApiKeyCreationResult(apiKey.getId(), name, type, rawToken, scopes, expiresAt);
  }

  /** Validates a raw API key token and returns the matching ApiKey if active. */
  @Transactional
  public ApiKey validateToken(String rawToken) {
    String tokenHash = sha256Hex(rawToken);
    ApiKey key = apiKeyRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new AccessDeniedException("Invalid API key"));
    if (!key.isActive()) {
      throw new AccessDeniedException("API key is revoked or expired");
    }
    key.setLastUsedAt(Instant.now());
    apiKeyRepository.save(key);
    return key;
  }

  /** Revokes a single key by its ID. Caller must belong to the organization. */
  @Transactional
  public void revokeApiKey(UUID organizationId, UUID keyId) {
    ApiKey key = apiKeyRepository.findById(keyId)
        .filter(k -> k.getOrganizationId().equals(organizationId))
        .orElseThrow(() -> new EntityNotFoundException("API key not found: " + keyId));
    key.setRevoked(true);
    apiKeyRepository.save(key);
    log.info("API key revoked: id={}, org={}", keyId, organizationId);
  }

  /** Lists all non-revoked API keys for an organization. */
  @Transactional(readOnly = true)
  public List<ApiKey> listApiKeys(UUID organizationId) {
    return apiKeyRepository.findAllByOrganizationId(organizationId).stream()
        .filter(k -> !k.isRevoked()).toList();
  }

  private String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(
          digest.digest(input.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** Carries the newly generated key's ID and the one-time plaintext token. */
  public record ApiKeyCreationResult(
      UUID id, String name, ApiKeyType type,
      String token, List<String> scopes, Instant expiresAt) {}
}

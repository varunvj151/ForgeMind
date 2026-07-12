package com.forgemind.modules.plugins.controller;

import com.forgemind.modules.plugins.entity.Plugin;
import com.forgemind.modules.plugins.repository.PluginRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/plugins")
@RequiredArgsConstructor
public class PluginController {

  private final PluginRepository pluginRepository;

  public record UploadPluginRequest(
      @NotBlank String pluginId,
      @NotBlank String version,
      @NotBlank String name,
      String description,
      @NotBlank String entrypoint // JavaScript code
  ) {}

  public record PluginResponse(
      UUID id, String pluginId, String version, String name, boolean active
  ) {}

  @PostMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<PluginResponse> uploadPlugin(
      @PathVariable UUID orgId,
      @Valid @RequestBody UploadPluginRequest request) {

    Plugin plugin = Plugin.builder()
        .organizationId(orgId)
        .pluginId(request.pluginId())
        .version(request.version())
        .name(request.name())
        .description(request.description())
        .entrypoint(request.entrypoint())
        .active(false) // Installed but disabled by default
        .build();

    plugin = pluginRepository.save(plugin);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new PluginResponse(
            plugin.getId(), plugin.getPluginId(), plugin.getVersion(), 
            plugin.getName(), plugin.isActive()));
  }

  @GetMapping
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<List<PluginResponse>> listPlugins(@PathVariable UUID orgId) {
    List<PluginResponse> responses = pluginRepository.findAllByOrganizationId(orgId).stream()
        .map(p -> new PluginResponse(p.getId(), p.getPluginId(), p.getVersion(), p.getName(), p.isActive()))
        .toList();
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/{pluginId}/toggle")
  @PreAuthorize("@orgSecurity.hasRole(#orgId, 'ADMIN')")
  public ResponseEntity<Void> togglePlugin(
      @PathVariable UUID orgId, @PathVariable UUID pluginId, @RequestParam boolean active) {
    
    pluginRepository.findById(pluginId)
        .filter(p -> p.getOrganizationId().equals(orgId))
        .ifPresent(p -> {
          p.setActive(active);
          pluginRepository.save(p);
        });
        
    return ResponseEntity.noContent().build();
  }
}

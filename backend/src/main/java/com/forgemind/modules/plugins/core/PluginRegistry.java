package com.forgemind.modules.plugins.core;

import com.forgemind.modules.plugins.entity.Plugin;
import com.forgemind.modules.plugins.repository.PluginRepository;
import com.forgemind.modules.plugins.sandbox.PluginSandbox;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Manages loading, unloading, and executing plugins.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginRegistry {

  private final PluginRepository pluginRepository;
  private final PluginSandbox sandbox;

  /**
   * Executes a specific extension point for all active plugins in an organization.
   */
  public void executeExtensionPoint(UUID organizationId, String extensionPointId, String payloadJson) {
    List<Plugin> activePlugins = pluginRepository.findAllByOrganizationIdAndActiveTrue(organizationId);

    for (Plugin plugin : activePlugins) {
      log.debug("Executing plugin {} for extension point {}", plugin.getPluginId(), extensionPointId);
      
      // Inject the extension point routing into the plugin script
      String script = String.format(
          "(function() { " +
          "  %s " +
          "  if (typeof onExtensionPoint === 'function') { " +
          "    return onExtensionPoint('%s', %s); " +
          "  } " +
          "  return null; " +
          "})();",
          plugin.getEntrypoint(), extensionPointId, payloadJson
      );

      try {
        sandbox.executeSafely(script, Duration.ofMillis(500)); // 500ms strict timeout
      } catch (Exception e) {
        log.warn("Plugin {} execution failed or timed out: {}", plugin.getPluginId(), e.getMessage());
      }
    }
  }
}

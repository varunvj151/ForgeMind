package com.forgemind.modules.plugins.core;

import java.util.Map;

/**
 * Defines a point in the platform where plugins can inject custom logic.
 */
public interface ExtensionPoint {

  /** Unique identifier for the extension point (e.g. "task.validate"). */
  String getExtensionPointId();

  /** Executes the plugin logic for this extension point. */
  Object execute(Map<String, Object> contextPayload);
}

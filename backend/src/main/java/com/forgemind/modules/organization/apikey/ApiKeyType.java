package com.forgemind.modules.organization.apikey;

/** Type of API key controlling its scope and usage context. */
public enum ApiKeyType {
  /** Tied to a specific user — scoped to their permissions. */
  PERSONAL,
  /** Scoped to a workspace — used by workspace-level automations. */
  WORKSPACE,
  /** Machine-to-machine — service accounts and CI/CD integrations. */
  SERVICE
}

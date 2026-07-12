package com.forgemind.modules.organization.security;

import java.util.UUID;

/**
 * Thread-local store for the current organization (tenant) context.
 *
 * <p>Set by {@link TenantResolutionFilter} at the start of each HTTP request and cleared
 * immediately after the response is committed. Never access this directly from business logic;
 * use {@link TenantContextHolder} instead.
 */
public final class TenantContext {

  private static final ThreadLocal<UUID> CURRENT_ORG = new ThreadLocal<>();

  private TenantContext() {}

  public static void setOrganizationId(UUID organizationId) {
    CURRENT_ORG.set(organizationId);
  }

  public static UUID getOrganizationId() {
    return CURRENT_ORG.get();
  }

  public static void clear() {
    CURRENT_ORG.remove();
  }

  /** Returns true when a tenant has been resolved for this request. */
  public static boolean hasOrganization() {
    return CURRENT_ORG.get() != null;
  }
}

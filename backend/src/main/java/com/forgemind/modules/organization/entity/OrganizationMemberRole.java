package com.forgemind.modules.organization.entity;

/** Role of a user within an organization. Controls what actions they may perform. */
public enum OrganizationMemberRole {
  /** Full control: billing, members, all settings. Only one owner per org. */
  OWNER,
  /** Manage members, projects, workspaces. Cannot access billing. */
  ADMIN,
  /** Create/manage projects and tasks within assigned workspaces. */
  MANAGER,
  /** Create tasks, contribute to projects. */
  MEMBER,
  /** Read-only access. */
  VIEWER
}

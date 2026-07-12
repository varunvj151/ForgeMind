package com.forgemind.modules.organization.entity;

/** State machine for organization invitations. */
public enum InvitationStatus {
  PENDING,
  ACCEPTED,
  EXPIRED,
  CANCELLED
}

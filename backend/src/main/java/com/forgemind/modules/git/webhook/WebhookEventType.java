package com.forgemind.modules.git.webhook;

public enum WebhookEventType {
  PUSH,
  PULL_REQUEST,
  RELEASE,
  BRANCH_CREATED,
  BRANCH_DELETED,
  UNKNOWN
}

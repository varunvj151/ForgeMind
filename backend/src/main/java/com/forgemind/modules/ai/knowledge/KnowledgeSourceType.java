package com.forgemind.modules.ai.knowledge;

/** Identifies the originating domain entity type for a knowledge document. */
public enum KnowledgeSourceType {
  PROJECT,
  TASK,
  ACTIVITY,
  TEAM,
  DOCUMENTATION,
  /** A connected Git repository. */
  REPOSITORY,
  /** A single indexed source file chunk from a repository. */
  SOURCE_FILE,
  /** A Git commit message and diff summary. */
  COMMIT,
  /** A Git pull request body and review comments. */
  PULL_REQUEST
}

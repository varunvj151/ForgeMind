package com.forgemind.modules.git.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized configuration for the Git integration module.
 *
 * <p>All values can be overridden via environment variables following the
 * {@code APP_GIT_*} convention.
 */
@Component
@ConfigurationProperties(prefix = "app.git")
@Getter
@Setter
public class GitProperties {

  /** Maximum number of commits to fetch per sync. Prevents overwhelming the DB on first sync. */
  private int maxCommitsPerSync = 100;

  /**
   * Maximum number of files to index per repository sync. Protects against huge mono-repos.
   */
  private int maxFilesPerSync = 500;

  /** Maximum file size in bytes to index. Files larger than this are skipped. */
  private long maxFileSizeBytes = 102_400; // 100 KB

  /** Whether the GitHub webhook signature validation is enforced (disable only in dev/test). */
  private boolean webhookSignatureRequired = true;

  /** Comma-separated file extensions that are ALWAYS excluded from indexing. */
  private String excludedExtensions = "class,jar,war,ear,zip,tar,gz,png,jpg,gif,svg,ico,woff,woff2,ttf,eot";
}

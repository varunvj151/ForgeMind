package com.forgemind.modules.git.provider;

import com.forgemind.modules.git.entity.GitBranch;
import com.forgemind.modules.git.entity.GitCommit;
import com.forgemind.modules.git.entity.GitPullRequest;
import com.forgemind.modules.git.entity.GitRepository;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction for Git provider APIs (GitHub, GitLab, Bitbucket).
 */
public interface GitProvider {
  
  /** Identifies which provider type this implementation handles. */
  GitProviderType getType();

  /** Validates the access token and returns repository metadata. */
  GitRepository getRepository(String owner, String repoName, String accessToken);

  /** Lists branches for a repository. */
  List<GitBranch> listBranches(GitRepository repository);

  /** Lists recent commits, optionally since a specific SHA to support incremental sync. */
  List<GitCommit> listCommits(GitRepository repository, String sinceSha, int limit);

  /** Lists recent pull requests. */
  List<GitPullRequest> listPullRequests(GitRepository repository, int limit);

  /** Fetches the raw content of a file at a specific commit or branch. */
  Optional<String> getFileContent(GitRepository repository, String filePath, String ref);

  /** Lists all files in the repository at a specific commit or branch (tree). */
  List<String> listFiles(GitRepository repository, String ref);
}

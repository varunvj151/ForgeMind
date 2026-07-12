package com.forgemind.modules.git.provider;

import com.forgemind.modules.git.entity.GitBranch;
import com.forgemind.modules.git.entity.GitCommit;
import com.forgemind.modules.git.entity.GitPullRequest;
import com.forgemind.modules.git.entity.GitRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Mock Git provider for testing.
 */
@Component
public class MockGitProvider implements GitProvider {

  @Override
  public GitProviderType getType() {
    return GitProviderType.MOCK;
  }

  @Override
  public GitRepository getRepository(String owner, String repoName, String accessToken) {
    return GitRepository.builder()
        .id(UUID.randomUUID())
        .provider(GitProviderType.MOCK)
        .owner(owner)
        .repoName(repoName)
        .fullName(owner + "/" + repoName)
        .defaultBranch("main")
        .cloneUrl("https://mock.git/" + owner + "/" + repoName + ".git")
        .visibility("PRIVATE")
        .primaryLanguage("Java")
        .description("Mock repository for testing")
        .accessToken(accessToken)
        .build();
  }

  @Override
  public List<GitBranch> listBranches(GitRepository repository) {
    return List.of(
        GitBranch.builder()
            .repository(repository)
            .name("main")
            .commitSha("mocksha1234567890abcdef1234567890abcdef12")
            .isDefault(true)
            .isProtected(true)
            .build());
  }

  @Override
  public List<GitCommit> listCommits(GitRepository repository, String sinceSha, int limit) {
    return List.of(
        GitCommit.builder()
            .repository(repository)
            .sha("mocksha1234567890abcdef1234567890abcdef12")
            .message("Initial mock commit")
            .authorName("Mock User")
            .authorEmail("mock@example.com")
            .authoredAt(Instant.now())
            .branchName("main")
            .filesChanged(5)
            .additions(100)
            .deletions(0)
            .build());
  }

  @Override
  public List<GitPullRequest> listPullRequests(GitRepository repository, int limit) {
    return List.of(
        GitPullRequest.builder()
            .repository(repository)
            .prNumber(1)
            .title("Mock Pull Request")
            .description("This is a mock PR")
            .state("OPEN")
            .authorLogin("Mock User")
            .sourceBranch("feature/mock-feature")
            .targetBranch("main")
            .filesChanged(2)
            .additions(50)
            .deletions(10)
            .build());
  }

  @Override
  public Optional<String> getFileContent(GitRepository repository, String filePath, String ref) {
    if (filePath.endsWith(".java")) {
      return Optional.of("package mock;\n\npublic class Mock {\n  // Mock Java file\n}");
    }
    return Optional.of("Mock content for " + filePath);
  }

  @Override
  public List<String> listFiles(GitRepository repository, String ref) {
    return List.of("src/main/java/mock/Mock.java", "README.md", "pom.xml");
  }
}

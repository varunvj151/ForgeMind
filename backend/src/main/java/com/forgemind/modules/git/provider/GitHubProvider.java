package com.forgemind.modules.git.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.forgemind.modules.git.entity.GitBranch;
import com.forgemind.modules.git.entity.GitCommit;
import com.forgemind.modules.git.entity.GitPullRequest;
import com.forgemind.modules.git.entity.GitRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * GitHub implementation of the GitProvider interface using Spring 6.1 RestClient.
 */
@Component
public class GitHubProvider implements GitProvider {

  private static final String GITHUB_API_URL = "https://api.github.com";

  private final RestClient restClient;

  public GitHubProvider(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder
        .baseUrl(GITHUB_API_URL)
        .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
        .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
        .build();
  }

  @Override
  public GitProviderType getType() {
    return GitProviderType.GITHUB;
  }

  @Override
  public GitRepository getRepository(String owner, String repoName, String accessToken) {
    JsonNode response = restClient.get()
        .uri("/repos/{owner}/{repo}", owner, repoName)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .retrieve()
        .body(JsonNode.class);

    if (response == null) {
      throw new RuntimeException("Failed to fetch repository from GitHub");
    }

    return GitRepository.builder()
        .provider(GitProviderType.GITHUB)
        .owner(owner)
        .repoName(repoName)
        .fullName(response.path("full_name").asText())
        .defaultBranch(response.path("default_branch").asText("main"))
        .cloneUrl(response.path("clone_url").asText())
        .visibility(response.path("visibility").asText("PRIVATE").toUpperCase())
        .primaryLanguage(response.path("language").asText(null))
        .description(response.path("description").asText(null))
        .accessToken(accessToken)
        .build();
  }

  @Override
  public List<GitBranch> listBranches(GitRepository repository) {
    JsonNode response = restClient.get()
        .uri("/repos/{owner}/{repo}/branches", repository.getOwner(), repository.getRepoName())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + repository.getAccessToken())
        .retrieve()
        .body(JsonNode.class);

    if (response == null || !response.isArray()) return List.of();

    List<GitBranch> branches = new ArrayList<>();
    for (JsonNode node : response) {
      String name = node.path("name").asText();
      boolean isDefault = name.equals(repository.getDefaultBranch());
      branches.add(GitBranch.builder()
          .repository(repository)
          .name(name)
          .commitSha(node.path("commit").path("sha").asText())
          .isDefault(isDefault)
          .isProtected(node.path("protected").asBoolean(false))
          .build());
    }
    return branches;
  }

  @Override
  public List<GitCommit> listCommits(GitRepository repository, String sinceSha, int limit) {
    String uri = "/repos/{owner}/{repo}/commits?per_page={limit}";
    if (sinceSha != null) {
        // Technically GitHub API doesn't support 'since SHA', it supports 'sha' (start) 
        // or 'since' (timestamp). We will fetch limit and just stop if we see sinceSha
        // For simplicity in this implementation, we just fetch recent.
    }

    JsonNode response = restClient.get()
        .uri(uri, repository.getOwner(), repository.getRepoName(), limit)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + repository.getAccessToken())
        .retrieve()
        .body(JsonNode.class);

    if (response == null || !response.isArray()) return List.of();

    List<GitCommit> commits = new ArrayList<>();
    for (JsonNode node : response) {
      String sha = node.path("sha").asText();
      if (sha.equals(sinceSha)) {
          break; // Stop when we hit the last synced commit
      }
      
      JsonNode commitNode = node.path("commit");
      JsonNode authorNode = commitNode.path("author");
      
      commits.add(GitCommit.builder()
          .repository(repository)
          .sha(sha)
          .message(commitNode.path("message").asText())
          .authorName(authorNode.path("name").asText())
          .authorEmail(authorNode.path("email").asText())
          .authoredAt(Instant.parse(authorNode.path("date").asText()))
          .build());
    }
    return commits;
  }

  @Override
  public List<GitPullRequest> listPullRequests(GitRepository repository, int limit) {
    JsonNode response = restClient.get()
        .uri("/repos/{owner}/{repo}/pulls?state=all&per_page={limit}", 
             repository.getOwner(), repository.getRepoName(), limit)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + repository.getAccessToken())
        .retrieve()
        .body(JsonNode.class);

    if (response == null || !response.isArray()) return List.of();

    List<GitPullRequest> prs = new ArrayList<>();
    for (JsonNode node : response) {
      String state = node.path("state").asText().toUpperCase();
      Instant mergedAt = node.path("merged_at").isNull() ? null : Instant.parse(node.path("merged_at").asText());
      Instant closedAt = node.path("closed_at").isNull() ? null : Instant.parse(node.path("closed_at").asText());
      if (mergedAt != null) state = "MERGED";

      prs.add(GitPullRequest.builder()
          .repository(repository)
          .prNumber(node.path("number").asInt())
          .title(node.path("title").asText())
          .description(node.path("body").asText(null))
          .state(state)
          .authorLogin(node.path("user").path("login").asText())
          .sourceBranch(node.path("head").path("ref").asText())
          .targetBranch(node.path("base").path("ref").asText())
          .mergedAt(mergedAt)
          .closedAt(closedAt)
          .build());
    }
    return prs;
  }

  @Override
  public Optional<String> getFileContent(GitRepository repository, String filePath, String ref) {
      try {
        ResponseEntity<String> response = restClient.get()
            .uri("/repos/{owner}/{repo}/contents/{path}?ref={ref}", 
                 repository.getOwner(), repository.getRepoName(), filePath, ref)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + repository.getAccessToken())
            .header(HttpHeaders.ACCEPT, "application/vnd.github.v3.raw")
            .retrieve()
            .toEntity(String.class);
            
        return Optional.ofNullable(response.getBody());
      } catch (Exception e) {
          // File might not exist or other error
          return Optional.empty();
      }
  }

  @Override
  public List<String> listFiles(GitRepository repository, String ref) {
      try {
        JsonNode response = restClient.get()
            .uri("/repos/{owner}/{repo}/git/trees/{ref}?recursive=1", 
                 repository.getOwner(), repository.getRepoName(), ref)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + repository.getAccessToken())
            .retrieve()
            .body(JsonNode.class);

        if (response == null || !response.has("tree") || !response.path("tree").isArray()) {
            return List.of();
        }

        List<String> files = new ArrayList<>();
        for (JsonNode node : response.path("tree")) {
            if ("blob".equals(node.path("type").asText())) {
                files.add(node.path("path").asText());
            }
        }
        return files;
      } catch (Exception e) {
          return List.of();
      }
  }
}

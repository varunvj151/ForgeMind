package com.forgemind.modules.git.service;

import com.forgemind.modules.git.config.GitMetrics;
import com.forgemind.modules.git.config.GitProperties;
import com.forgemind.modules.git.entity.GitBranch;
import com.forgemind.modules.git.entity.GitCommit;
import com.forgemind.modules.git.entity.GitPullRequest;
import com.forgemind.modules.git.entity.GitRepository;
import com.forgemind.modules.git.provider.GitProvider;
import com.forgemind.modules.git.provider.GitProviderFactory;
import com.forgemind.modules.git.repository.GitBranchRepository;
import com.forgemind.modules.git.repository.GitCommitRepository;
import com.forgemind.modules.git.repository.GitPullRequestRepository;
import com.forgemind.modules.git.repository.GitRepositoryRepository;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GitSyncServiceImpl implements GitSyncService {

  private final GitRepositoryRepository gitRepositoryRepository;
  private final GitBranchRepository gitBranchRepository;
  private final GitCommitRepository gitCommitRepository;
  private final GitPullRequestRepository gitPullRequestRepository;
  private final GitProviderFactory gitProviderFactory;
  private final CodeIndexingService codeIndexingService;
  private final GitProperties gitProperties;
  private final GitMetrics gitMetrics;

  public GitSyncServiceImpl(
      GitRepositoryRepository gitRepositoryRepository,
      GitBranchRepository gitBranchRepository,
      GitCommitRepository gitCommitRepository,
      GitPullRequestRepository gitPullRequestRepository,
      GitProviderFactory gitProviderFactory,
      CodeIndexingService codeIndexingService,
      GitProperties gitProperties,
      GitMetrics gitMetrics) {
    this.gitRepositoryRepository = gitRepositoryRepository;
    this.gitBranchRepository = gitBranchRepository;
    this.gitCommitRepository = gitCommitRepository;
    this.gitPullRequestRepository = gitPullRequestRepository;
    this.gitProviderFactory = gitProviderFactory;
    this.codeIndexingService = codeIndexingService;
    this.gitProperties = gitProperties;
    this.gitMetrics = gitMetrics;
  }

  @Override
  @Transactional
  public void syncRepository(UUID repositoryId) {
    Timer.Sample sample = gitMetrics.startSyncTimer();
    GitRepository repo = gitRepositoryRepository.findById(repositoryId)
        .orElseThrow(() -> new IllegalArgumentException("Repo not found"));
        
    try {
      GitProvider provider = gitProviderFactory.getProvider(repo.getProvider());
      
      // Sync Branches
      List<GitBranch> branches = provider.listBranches(repo);
      gitBranchRepository.deleteByRepositoryId(repo.getId());
      gitBranchRepository.saveAll(branches);

      // Sync Commits
      List<GitCommit> commits = provider.listCommits(repo, repo.getLastCommitSha(), gitProperties.getMaxCommitsPerSync());
      for (GitCommit commit : commits) {
         if (gitCommitRepository.findByRepositoryIdAndSha(repo.getId(), commit.getSha()).isEmpty()) {
             gitCommitRepository.save(commit);
         }
      }

      // Sync PRs
      List<GitPullRequest> prs = provider.listPullRequests(repo, 50);
      for (GitPullRequest pr : prs) {
          GitPullRequest existing = gitPullRequestRepository.findByRepositoryIdAndPrNumber(repo.getId(), pr.getPrNumber()).orElse(null);
          if (existing != null) {
              existing.setState(pr.getState());
              existing.setTitle(pr.getTitle());
              existing.setMergedAt(pr.getMergedAt());
              existing.setClosedAt(pr.getClosedAt());
              gitPullRequestRepository.save(existing);
          } else {
              gitPullRequestRepository.save(pr);
          }
      }

      if (!commits.isEmpty()) {
          repo.setLastCommitSha(commits.get(0).getSha());
      }
      repo.setLastSyncAt(Instant.now());
      gitRepositoryRepository.save(repo);

      // Trigger indexing asynchronously
      codeIndexingService.indexRepositoryAsync(repo.getId());

      gitMetrics.recordSync(sample, repo.getProvider().name(), "SUCCESS");
    } catch (Exception e) {
      gitMetrics.recordSync(sample, repo.getProvider().name(), "FAILURE");
      log.error("Failed to sync repository {}", repo.getFullName(), e);
      throw e;
    }
  }

  @Async
  @Override
  public CompletableFuture<Void> syncRepositoryAsync(UUID repositoryId) {
    syncRepository(repositoryId);
    return CompletableFuture.completedFuture(null);
  }
}

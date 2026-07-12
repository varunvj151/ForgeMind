package com.forgemind.modules.git.repository;

import com.forgemind.modules.git.entity.GitPullRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitPullRequestRepository extends JpaRepository<GitPullRequest, UUID> {
  List<GitPullRequest> findAllByRepositoryIdOrderByPrNumberDesc(UUID repositoryId);
  Optional<GitPullRequest> findByRepositoryIdAndPrNumber(UUID repositoryId, int prNumber);
  void deleteByRepositoryId(UUID repositoryId);
}

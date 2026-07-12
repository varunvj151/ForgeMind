package com.forgemind.modules.git.repository;

import com.forgemind.modules.git.entity.GitCommit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitCommitRepository extends JpaRepository<GitCommit, UUID> {
  List<GitCommit> findAllByRepositoryIdOrderByAuthoredAtDesc(UUID repositoryId);
  Optional<GitCommit> findByRepositoryIdAndSha(UUID repositoryId, String sha);
  void deleteByRepositoryId(UUID repositoryId);
}

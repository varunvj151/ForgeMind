package com.forgemind.modules.git.repository;

import com.forgemind.modules.git.entity.GitRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepository, UUID> {
  List<GitRepository> findAllByProjectId(UUID projectId);
  Optional<GitRepository> findByProviderAndOwnerAndRepoName(String provider, String owner, String repoName);
}

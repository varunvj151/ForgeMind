package com.forgemind.modules.git.repository;

import com.forgemind.modules.git.entity.GitBranch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitBranchRepository extends JpaRepository<GitBranch, UUID> {
  List<GitBranch> findAllByRepositoryId(UUID repositoryId);
  Optional<GitBranch> findByRepositoryIdAndName(UUID repositoryId, String name);
  void deleteByRepositoryId(UUID repositoryId);
}

package com.forgemind.modules.git.repository;

import com.forgemind.modules.git.entity.CodeChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeChunkRepository extends JpaRepository<CodeChunk, UUID> {
  List<CodeChunk> findAllByRepositoryId(UUID repositoryId);
  void deleteByRepositoryId(UUID repositoryId);
  void deleteByRepositoryIdAndFilePath(UUID repositoryId, String filePath);
  
  // Note: Vector search itself uses a native query in CodeSearchServiceImpl, 
  // not this standard JPA repository method.
}

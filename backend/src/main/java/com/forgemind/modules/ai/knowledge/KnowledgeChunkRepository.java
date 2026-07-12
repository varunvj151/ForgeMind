package com.forgemind.modules.ai.knowledge;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Spring Data JPA repository for {@link KnowledgeChunk}. */
@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {

  List<KnowledgeChunk> findByDocumentId(UUID documentId);

  @Transactional
  @Modifying
  void deleteByDocumentId(UUID documentId);

  @Transactional
  @Modifying
  @Query("DELETE FROM KnowledgeChunk c WHERE c.projectId = :projectId")
  void deleteByProjectId(@Param("projectId") UUID projectId);

  long countByProjectId(UUID projectId);

  /**
   * Counts chunks whose embedding has not yet been generated.
   * Uses a native query because the embedding column is not mapped in the JPA entity.
   */
  @Query(
      value = "SELECT COUNT(*) FROM knowledge_chunks WHERE project_id = :projectId AND embedding IS NULL",
      nativeQuery = true)
  long countPendingEmbeddingByProjectId(@Param("projectId") UUID projectId);
}

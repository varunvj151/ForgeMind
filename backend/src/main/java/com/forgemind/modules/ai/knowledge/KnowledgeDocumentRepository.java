package com.forgemind.modules.ai.knowledge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Spring Data JPA repository for {@link KnowledgeDocument}. */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {

  Optional<KnowledgeDocument> findBySourceTypeAndSourceId(
      KnowledgeSourceType sourceType, String sourceId);

  List<KnowledgeDocument> findByProjectId(UUID projectId);

  @Transactional
  @Modifying
  @Query("DELETE FROM KnowledgeDocument d WHERE d.projectId = :projectId")
  void deleteByProjectId(@Param("projectId") UUID projectId);

  long countByProjectId(UUID projectId);
}

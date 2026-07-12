package com.forgemind.modules.ai.retrieval;

import com.forgemind.modules.ai.knowledge.KnowledgeSourceType;
import java.util.UUID;
import lombok.Builder;

/**
 * Encapsulates the parameters for a semantic knowledge search.
 *
 * @param projectId  mandatory project scope for authorization
 * @param queryText  the natural language question or keywords
 * @param sourceType optional filter to restrict search to tasks, activities, etc.
 * @param topK       maximum number of chunks to retrieve
 * @param minScore   minimum cosine similarity score threshold (0.0 to 1.0)
 */
@Builder
public record RetrievalQuery(
    UUID projectId,
    String queryText,
    KnowledgeSourceType sourceType,
    int topK,
    double minScore) {

  public RetrievalQuery {
    if (projectId == null) {
      throw new IllegalArgumentException("projectId is mandatory for retrieval");
    }
    if (queryText == null || queryText.isBlank()) {
      throw new IllegalArgumentException("queryText cannot be empty");
    }
    if (topK <= 0) {
      topK = 5;
    }
    if (minScore < 0.0 || minScore > 1.0) {
      minScore = 0.6;
    }
  }
}

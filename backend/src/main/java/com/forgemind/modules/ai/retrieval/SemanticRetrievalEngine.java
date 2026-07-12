package com.forgemind.modules.ai.retrieval;

import com.forgemind.modules.ai.embedding.EmbeddingProvider;
import com.forgemind.modules.ai.observability.KnowledgeMetrics;
import com.forgemind.modules.ai.vector.ScoredChunk;
import com.forgemind.modules.ai.vector.VectorStore;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link RetrievalEngine}.
 *
 * <p>Orchestrates the query embedding via {@link EmbeddingProvider} and the similarity search via
 * {@link VectorStore}, filtering out chunks below the configured relevance threshold.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticRetrievalEngine implements RetrievalEngine {

  private final EmbeddingProvider embeddingProvider;
  private final VectorStore vectorStore;
  private final KnowledgeMetrics metrics;

  @Override
  public RetrievalResult retrieve(RetrievalQuery query) {
    long start = System.currentTimeMillis();
    String provider = embeddingProvider.providerName();

    try {
      // 1. Embed the user's natural language query
      float[] queryVector = embeddingProvider.embed(query.queryText());

      // 2. Perform vector similarity search
      List<ScoredChunk> allResults =
          vectorStore.searchWithFilter(
              queryVector, query.projectId(), query.sourceType(), query.topK());

      // 3. Filter by minimum score
      List<ScoredChunk> filtered =
          allResults.stream()
              .filter(sc -> sc.score() >= query.minScore())
              .toList();

      long elapsed = System.currentTimeMillis() - start;
      metrics.recordRetrievalSuccess(provider, elapsed, filtered.size());

      log.debug(
          "Retrieved {} chunks (from {} total) in {}ms for query '{}' in project {}",
          filtered.size(),
          allResults.size(),
          elapsed,
          query.queryText(),
          query.projectId());

      return new RetrievalResult(filtered, elapsed, provider);

    } catch (Exception e) {
      metrics.recordRetrievalFailure(provider, e.getClass().getSimpleName());
      log.error("Retrieval failed for project {}: {}", query.projectId(), e.getMessage(), e);
      throw e;
    }
  }
}

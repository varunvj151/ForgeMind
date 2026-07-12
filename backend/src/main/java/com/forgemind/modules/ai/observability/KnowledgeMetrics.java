package com.forgemind.modules.ai.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Micrometer metrics for the knowledge base, vector store, and RAG subsystem.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeMetrics {

  private final MeterRegistry registry;

  public void recordEmbeddingSuccess(String provider, long elapsedMs, int batchSize) {
    Timer.builder("forgemind.ai.embedding.duration")
        .tag("provider", provider)
        .tag("status", "success")
        .description("Time taken to generate and persist embeddings")
        .register(registry)
        .record(elapsedMs, TimeUnit.MILLISECONDS);

    registry.counter("forgemind.ai.embedding.chunks", "provider", provider, "status", "success")
        .increment(batchSize);
  }

  public void recordEmbeddingFailure(String provider, String errorType) {
    registry.counter("forgemind.ai.embedding.failures", "provider", provider, "error", errorType).increment();
  }

  public void recordRetrievalSuccess(String provider, long elapsedMs, int chunksRetrieved) {
    Timer.builder("forgemind.ai.retrieval.duration")
        .tag("provider", provider)
        .tag("status", "success")
        .description("Time taken for vector similarity search")
        .register(registry)
        .record(elapsedMs, TimeUnit.MILLISECONDS);

    registry.counter("forgemind.ai.retrieval.chunks", "provider", provider, "status", "success")
        .increment(chunksRetrieved);
  }

  public void recordRetrievalFailure(String provider, String errorType) {
    registry.counter("forgemind.ai.retrieval.failures", "provider", provider, "error", errorType).increment();
  }
}

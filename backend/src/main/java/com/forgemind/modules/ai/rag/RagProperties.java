package com.forgemind.modules.ai.rag;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Typed configuration properties for RAG orchestration, bound from {@code app.rag.*}. */
@Component
@ConfigurationProperties(prefix = "app.rag")
@Getter
@Setter
public class RagProperties {

  /** Master switch to enable or disable RAG augmentation across all agents. */
  private boolean enabled = true;

  /** Number of chunks to retrieve per query. */
  private int topK = 5;

  /** Minimum cosine similarity score threshold (0.0 to 1.0) for a chunk to be injected. */
  private double minScore = 0.6;
}

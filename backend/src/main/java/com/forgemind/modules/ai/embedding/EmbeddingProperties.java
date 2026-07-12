package com.forgemind.modules.ai.embedding;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed configuration properties for the embedding subsystem, bound from {@code app.embedding.*}.
 */
@Component
@ConfigurationProperties(prefix = "app.embedding")
@Getter
@Setter
public class EmbeddingProperties {

  /** Active embedding provider: MOCK, GEMINI, OPENAI, or OLLAMA. */
  private String provider = "MOCK";

  /** Number of embedding dimensions. Must match the {@code vector(N)} column in V9 migration. */
  private int dimensions = 1536;

  /** Maximum number of texts to embed in a single batch API call. */
  private int batchSize = 32;

  /** Base URL for a local Ollama instance. */
  private String ollamaBaseUrl = "http://localhost:11434";

  /** Model name for Ollama embeddings (e.g. nomic-embed-text, mxbai-embed-large). */
  private String ollamaModel = "nomic-embed-text";
}

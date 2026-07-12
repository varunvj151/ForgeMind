package com.forgemind.modules.ai.embedding;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Deterministic mock embedding provider for development and testing.
 *
 * <p>Produces L2-normalized pseudo-random float vectors seeded from the input text's hash code,
 * so the same text always yields the same vector. This makes unit tests reproducible and allows
 * basic similarity comparisons without any external API calls.
 *
 * <p>Active when {@code app.embedding.provider=MOCK} (the default).
 */
@Component
@ConditionalOnProperty(name = "app.embedding.provider", havingValue = "MOCK", matchIfMissing = true)
@Slf4j
public class MockEmbeddingProvider implements EmbeddingProvider {

  private final int dimensions;

  public MockEmbeddingProvider(EmbeddingProperties properties) {
    this.dimensions = properties.getDimensions();
    log.info("MockEmbeddingProvider initialized with {} dimensions", dimensions);
  }

  @Override
  public String providerName() {
    return "MOCK";
  }

  @Override
  public float[] embed(String text) {
    int seed = text == null ? 0 : text.hashCode();
    Random rng = new Random(seed);
    float[] vector = new float[dimensions];
    for (int i = 0; i < dimensions; i++) {
      vector[i] = rng.nextFloat() * 2f - 1f; // uniform in [-1, 1]
    }
    return normalize(vector);
  }

  @Override
  public int dimensions() {
    return dimensions;
  }

  /**
   * L2-normalizes a vector in-place so it lies on the unit hypersphere. Cosine similarity between
   * unit vectors equals their dot product, which pgvector's {@code <=>} operator computes.
   */
  private float[] normalize(float[] vector) {
    double sumOfSquares = 0.0;
    for (float v : vector) {
      sumOfSquares += (double) v * v;
    }
    double norm = Math.sqrt(sumOfSquares);
    if (norm < 1e-10) {
      return vector; // zero vector — return as-is
    }
    float[] normalized = new float[vector.length];
    for (int i = 0; i < vector.length; i++) {
      normalized[i] = (float) (vector[i] / norm);
    }
    return normalized;
  }
}

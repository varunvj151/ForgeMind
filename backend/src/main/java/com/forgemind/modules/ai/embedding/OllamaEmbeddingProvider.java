package com.forgemind.modules.ai.embedding;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Embedding provider backed by a locally-running Ollama instance.
 *
 * <p>Calls the {@code /api/embeddings} endpoint. The model and base URL are configurable via
 * {@code app.embedding.ollama-model} and {@code app.embedding.ollama-base-url}.
 *
 * <p>Active when {@code app.embedding.provider=OLLAMA}.
 */
@Component
@ConditionalOnProperty(name = "app.embedding.provider", havingValue = "OLLAMA")
@Slf4j
public class OllamaEmbeddingProvider implements EmbeddingProvider {

  private final RestClient restClient;
  private final String model;
  private final int dimensions;

  public OllamaEmbeddingProvider(EmbeddingProperties properties) {
    this.model = properties.getOllamaModel();
    this.dimensions = properties.getDimensions();
    this.restClient = RestClient.builder().baseUrl(properties.getOllamaBaseUrl()).build();
    log.info(
        "OllamaEmbeddingProvider initialized — model={}, baseUrl={}, dims={}",
        model,
        properties.getOllamaBaseUrl(),
        dimensions);
  }

  @Override
  public String providerName() {
    return "OLLAMA";
  }

  @Override
  @SuppressWarnings("unchecked")
  public float[] embed(String text) {
    Map<String, Object> body = Map.of("model", model, "prompt", text);
    Map<String, Object> response =
        restClient.post().uri("/api/embeddings").body(body).retrieve().body(Map.class);

    List<Number> values = (List<Number>) response.get("embedding");
    float[] result = new float[values.size()];
    for (int i = 0; i < values.size(); i++) {
      result[i] = values.get(i).floatValue();
    }
    // Pad or truncate to configured dimension
    if (result.length != dimensions) {
      float[] adjusted = new float[dimensions];
      System.arraycopy(result, 0, adjusted, 0, Math.min(result.length, dimensions));
      return adjusted;
    }
    return result;
  }

  @Override
  public int dimensions() {
    return dimensions;
  }
}

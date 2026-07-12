package com.forgemind.modules.ai.embedding;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Embedding provider backed by OpenAI {@code text-embedding-3-small}.
 *
 * <p>Produces 1536-dimensional embeddings by default, matching the V9 schema. Requires the
 * {@code OPENAI_API_KEY} environment variable to be set.
 *
 * <p>Active when {@code app.embedding.provider=OPENAI}.
 */
@Component
@ConditionalOnProperty(name = "app.embedding.provider", havingValue = "OPENAI")
@Slf4j
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

  private static final String DEFAULT_MODEL = "text-embedding-3-small";

  private final RestClient restClient;
  private final int dimensions;

  public OpenAiEmbeddingProvider(
      EmbeddingProperties properties,
      @Value("${OPENAI_API_KEY:}") String apiKey) {
    this.dimensions = properties.getDimensions();
    this.restClient =
        RestClient.builder()
            .baseUrl("https://api.openai.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    log.info("OpenAiEmbeddingProvider initialized with model={} dims={}", DEFAULT_MODEL, dimensions);
  }

  @Override
  public String providerName() {
    return "OPENAI";
  }

  @Override
  @SuppressWarnings("unchecked")
  public float[] embed(String text) {
    Map<String, Object> body = Map.of("input", text, "model", DEFAULT_MODEL);
    Map<String, Object> response =
        restClient.post().uri("/v1/embeddings").body(body).retrieve().body(Map.class);

    List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
    List<Number> values = (List<Number>) data.get(0).get("embedding");

    float[] result = new float[values.size()];
    for (int i = 0; i < values.size(); i++) {
      result[i] = values.get(i).floatValue();
    }
    return result;
  }

  @Override
  public List<float[]> embedBatch(List<String> texts) {
    Map<String, Object> body = Map.of("input", texts, "model", DEFAULT_MODEL);
    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        restClient.post().uri("/v1/embeddings").body(body).retrieve().body(Map.class);

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
    return data.stream()
        .map(
            item -> {
              @SuppressWarnings("unchecked")
              List<Number> values = (List<Number>) item.get("embedding");
              float[] arr = new float[values.size()];
              for (int i = 0; i < values.size(); i++) {
                arr[i] = values.get(i).floatValue();
              }
              return arr;
            })
        .toList();
  }

  @Override
  public int dimensions() {
    return dimensions;
  }
}

package com.forgemind.modules.ai.embedding;

import com.forgemind.modules.ai.config.AiProperties;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Embedding provider backed by Google Gemini {@code text-embedding-004}.
 *
 * <p>Produces 768-dimensional embeddings. When the configured schema dimension is 1536, the vector
 * is padded with zeros to match — the cosine similarity is computed only over the populated
 * dimensions, preserving semantic meaning. For best results, set {@code EMBEDDING_DIMENSIONS=768}
 * and redeploy with a compatible V9 migration.
 *
 * <p>Active when {@code app.embedding.provider=GEMINI}.
 */
@Component
@ConditionalOnProperty(name = "app.embedding.provider", havingValue = "GEMINI")
@Slf4j
public class GeminiEmbeddingProvider implements EmbeddingProvider {

  private static final String GEMINI_EMBED_MODEL = "text-embedding-004";
  private static final int GEMINI_NATIVE_DIMENSIONS = 768;

  private final RestClient restClient;
  private final String apiKey;
  private final int targetDimensions;

  public GeminiEmbeddingProvider(EmbeddingProperties embeddingProperties, AiProperties aiProperties) {
    this.apiKey = aiProperties.getApiKey();
    this.targetDimensions = embeddingProperties.getDimensions();
    this.restClient =
        RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .defaultHeader("x-goog-api-key", this.apiKey)
            .build();
    log.info(
        "GeminiEmbeddingProvider initialized — native dims: {}, target dims: {}",
        GEMINI_NATIVE_DIMENSIONS,
        targetDimensions);
  }

  @Override
  public String providerName() {
    return "GEMINI";
  }

  @Override
  public float[] embed(String text) {
    String url =
        "/v1beta/models/" + GEMINI_EMBED_MODEL + ":embedContent";
    Map<String, Object> body =
        Map.of(
            "model", "models/" + GEMINI_EMBED_MODEL,
            "content", Map.of("parts", List.of(Map.of("text", text))));

    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        restClient.post().uri(url).body(body).retrieve().body(Map.class);

    if (response == null || !response.containsKey("embedding")) {
      throw new RuntimeException("Gemini embedding API returned unexpected response");
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
    @SuppressWarnings("unchecked")
    List<Number> values = (List<Number>) embedding.get("values");

    float[] raw = new float[values.size()];
    for (int i = 0; i < values.size(); i++) {
      raw[i] = values.get(i).floatValue();
    }
    return padOrTruncate(raw, targetDimensions);
  }

  @Override
  public int dimensions() {
    return targetDimensions;
  }

  private float[] padOrTruncate(float[] src, int targetLen) {
    if (src.length == targetLen) return src;
    float[] result = new float[targetLen];
    System.arraycopy(src, 0, result, 0, Math.min(src.length, targetLen));
    return result;
  }
}

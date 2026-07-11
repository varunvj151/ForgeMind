package com.forgemind.modules.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.forgemind.modules.ai.config.AiProperties;
import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;
import com.forgemind.modules.ai.exception.AiProviderException;
import com.forgemind.modules.ai.exception.AiRateLimitException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Slf4j
public class GeminiProvider implements AiProvider {

    private final RestClient restClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    public GeminiProvider(AiProperties aiProperties, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.restClient = RestClient.builder()
                .defaultHeader("x-goog-api-key", aiProperties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public AiResponse generate(AiRequest request) {
        String modelToUse = request.getModelOverride() != null ? request.getModelOverride() : aiProperties.getModel();
        Timer timer = meterRegistry.timer("ai.provider.request.duration", "provider", "GEMINI", "model", modelToUse);
        long start = System.currentTimeMillis();

        try {
            ObjectNode requestBody = buildRequestBody(request);

            JsonNode responseNode = restClient.post()
                    .uri(GEMINI_API_URL, modelToUse)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            throw new AiRateLimitException("Gemini API rate limit exceeded");
                        }
                        throw new AiProviderException("Client error from Gemini API: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new AiProviderException("Server error from Gemini API: " + res.getStatusCode());
                    })
                    .body(JsonNode.class);

            AiResponse aiResponse = parseResponse(responseNode, modelToUse);
            meterRegistry.counter("ai.provider.request.success", "provider", "GEMINI").increment();
            return aiResponse;

        } catch (AiProviderException | AiRateLimitException e) {
            meterRegistry.counter("ai.provider.request.failure", "provider", "GEMINI", "reason", e.getClass().getSimpleName()).increment();
            throw e;
        } catch (Exception e) {
            meterRegistry.counter("ai.provider.request.failure", "provider", "GEMINI", "reason", "Exception").increment();
            throw new AiProviderException("Failed to call Gemini API", e);
        } finally {
            timer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
        }
    }

    private ObjectNode buildRequestBody(AiRequest request) {
        ObjectNode root = objectMapper.createObjectNode();

        // System Instruction
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            ObjectNode sysInstruction = root.putObject("systemInstruction");
            ArrayNode parts = sysInstruction.putArray("parts");
            parts.addObject().put("text", request.getSystemPrompt());
        }

        // Contents
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        content.put("role", "user");
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", request.getUserPrompt());

        // Generation Config
        ObjectNode genConfig = root.putObject("generationConfig");
        genConfig.put("temperature", request.getTemperatureOverride() != null ? request.getTemperatureOverride() : aiProperties.getTemperature());
        genConfig.put("maxOutputTokens", aiProperties.getMaxTokens());
        if (request.isJsonMode()) {
            genConfig.put("responseMimeType", "application/json");
        }

        return root;
    }

    private AiResponse parseResponse(JsonNode responseNode, String model) {
        try {
            String content = responseNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            JsonNode usageMetadata = responseNode.path("usageMetadata");
            int promptTokens = usageMetadata.path("promptTokenCount").asInt(0);
            int completionTokens = usageMetadata.path("candidatesTokenCount").asInt(0);
            int totalTokens = usageMetadata.path("totalTokenCount").asInt(0);

            return AiResponse.builder()
                    .content(content)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .provider("GEMINI")
                    .model(model)
                    .build();
        } catch (Exception e) {
            throw new AiProviderException("Failed to parse Gemini API response", e);
        }
    }
}

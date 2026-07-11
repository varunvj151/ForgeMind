package com.forgemind.modules.ai.provider;

import com.forgemind.modules.ai.dto.AiRequest;
import com.forgemind.modules.ai.dto.AiResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MockAiProvider implements AiProvider {

    private final MeterRegistry meterRegistry;

    @Override
    public AiResponse generate(AiRequest request) {
        log.info("Mocking AI response for user prompt length: {}", request.getUserPrompt().length());

        Timer timer = meterRegistry.timer("ai.provider.request.duration", "provider", "MOCK");
        long start = System.currentTimeMillis();

        try {
            // Simulate network delay
            Thread.sleep(500);

            String mockContent;
            if (request.isJsonMode()) {
                mockContent = "{ \"message\": \"This is a mock JSON response from ForgeMind AI.\" }";
            } else {
                mockContent = "This is a mock text response from ForgeMind AI.";
            }

            meterRegistry.counter("ai.provider.request.success", "provider", "MOCK").increment();

            return AiResponse.builder()
                    .content(mockContent)
                    .promptTokens(10)
                    .completionTokens(20)
                    .totalTokens(30)
                    .provider("MOCK")
                    .model("mock-model-v1")
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            meterRegistry.counter("ai.provider.request.failure", "provider", "MOCK").increment();
            throw new RuntimeException("Mock AI interrupted", e);
        } finally {
            timer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
        }
    }
}

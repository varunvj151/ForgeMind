package com.forgemind.modules.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.modules.ai.provider.AiProvider;
import com.forgemind.modules.ai.provider.GeminiProvider;
import com.forgemind.modules.ai.provider.MockAiProvider;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Bean
    public AiProvider aiProvider() {
        String providerStr = aiProperties.getProvider().toUpperCase();
        log.info("Initializing AI Provider: {}", providerStr);

        return switch (providerStr) {
            case "GEMINI" -> new GeminiProvider(aiProperties, objectMapper, meterRegistry);
            case "MOCK" -> new MockAiProvider(meterRegistry);
            default -> {
                log.warn("Unknown AI Provider: {}. Falling back to MOCK.", providerStr);
                yield new MockAiProvider(meterRegistry);
            }
        };
    }
}

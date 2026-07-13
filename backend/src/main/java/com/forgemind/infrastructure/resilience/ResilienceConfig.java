package com.forgemind.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

  /** Circuit breaker for outbound AI provider calls. */
  @Bean("aiProviderCircuitBreakerConfig")
  public CircuitBreakerConfig aiProviderCircuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .permittedNumberOfCallsInHalfOpenState(3)
        .build();
  }

  /** Retry config with exponential backoff for webhook dispatch. */
  @Bean("webhookRetryConfig")
  public RetryConfig webhookRetryConfig() {
    return RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(500))
        .build();
  }

  /** Time limiter for AI requests (hard timeout). */
  @Bean("aiTimeLimiterConfig")
  public TimeLimiterConfig aiTimeLimiterConfig() {
    return TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(30))
        .build();
  }
}

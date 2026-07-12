package com.forgemind.modules.git.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Encapsulates Micrometer metrics for Git integration and indexing.
 *
 * <p>Tracks sync durations, indexing performance, and webhook events to ensure the platform
 * remains performant as repositories grow.
 */
@Component
public class GitMetrics {

  private final MeterRegistry registry;

  public GitMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  /** Starts a timer for repository synchronization. */
  public Timer.Sample startSyncTimer() {
    return Timer.start(registry);
  }

  /** Records the completion of a repository sync. */
  public void recordSync(Timer.Sample sample, String provider, String status) {
    sample.stop(
        Timer.builder("git.sync.duration")
            .description("Time taken to synchronize a git repository")
            .tag("provider", provider)
            .tag("status", status)
            .register(registry));
  }

  /** Records the number of files indexed in a sync run. */
  public void recordFilesIndexed(int count) {
    Counter.builder("git.index.files")
        .description("Number of source files successfully indexed")
        .register(registry)
        .increment(count);
  }

  /** Records the number of vector embeddings generated during indexing. */
  public void recordEmbeddingsGenerated(int count) {
    Counter.builder("git.index.embeddings")
        .description("Number of vector embeddings generated for code chunks")
        .register(registry)
        .increment(count);
  }

  /** Records an incoming webhook event. */
  public void recordWebhookEvent(String provider, String eventType) {
    Counter.builder("git.webhook.events")
        .description("Number of webhook events received")
        .tag("provider", provider)
        .tag("event", eventType)
        .register(registry)
        .increment();
  }
}

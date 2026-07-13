package com.forgemind.infrastructure.resilience;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GracefulShutdownConfig {

  @PreDestroy
  public void onShutdown() throws InterruptedException {
    log.info("Graceful shutdown initiated — draining in-flight requests...");
    // Allow time for in-flight requests to complete before container is terminated
    // Kubernetes terminationGracePeriodSeconds handles the hard kill
    Thread.sleep(5_000);
    log.info("Shutdown drain complete");
  }
}

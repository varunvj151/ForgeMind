package com.forgemind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ForgeMind Application Entry Point.
 *
 * <p>Bootstraps the Spring Boot application with:
 * <ul>
 *   <li>Component scanning across all {@code com.forgemind} packages</li>
 *   <li>Async task execution support (required for AI agent pipeline in Phase 4)</li>
 *   <li>Scheduling support (required for background jobs in Phase 2+)</li>
 * </ul>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ForgemindApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForgemindApplication.class, args);
    }
}

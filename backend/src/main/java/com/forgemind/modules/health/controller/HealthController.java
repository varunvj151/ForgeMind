package com.forgemind.modules.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check controller — publicly accessible, no authentication required.
 *
 * <p>Provides a lightweight liveness probe endpoint used by:
 * <ul>
 *   <li>Docker Compose {@code healthcheck} directives</li>
 *   <li>GitHub Actions CI pipeline smoke test</li>
 *   <li>Future Kubernetes liveness/readiness probes</li>
 * </ul>
 *
 * <p>For deeper health information (database connectivity, Redis ping), use the
 * Spring Boot Actuator endpoint at {@code GET /actuator/health}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Service health and liveness check")
public class HealthController {

    @GetMapping
    @Operation(summary = "Liveness check", description = "Returns OK when the application is running")
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health check requested");
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "forgemind-backend",
                "version", "v1",
                "timestamp", Instant.now().toString()
        ));
    }
}

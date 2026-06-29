package com.forgemind.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that require a real PostgreSQL database.
 *
 * <p>Uses Testcontainers to spin up a PostgreSQL container once for the entire test suite.
 * Subclasses should be annotated with the test they need.
 *
 * <p>The {@code test} Spring profile is activated so JWT and other app properties are available.
 * Redis is mocked so tests don't need a running Redis instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

  // Shared container — started once for all subclasses in the JVM
  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("forgemind_test")
          .withUsername("test")
          .withPassword("test");

  // Mock Redis — integration tests don't need a real Redis
  @MockBean RedisConnectionFactory redisConnectionFactory;

  @MockBean ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // Override the H2 datasource from application-test.yml with real Postgres
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    // Let Flyway run on the real Postgres container
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.url", postgres::getJdbcUrl);
    registry.add("spring.flyway.user", postgres::getUsername);
    registry.add("spring.flyway.password", postgres::getPassword);
    // Switch from Hibernate ddl-auto to none (Flyway manages the schema)
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }
}

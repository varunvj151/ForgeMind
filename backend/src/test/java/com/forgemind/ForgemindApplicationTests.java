package com.forgemind;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application context smoke test. Verifies that the Spring application context loads without
 * errors.
 *
 * <p>Redis is mocked so the test does not require a running Redis instance. H2 in-memory database
 * is used via the {@code test} profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class ForgemindApplicationTests {

  /** Mock Redis so the context loads without a running Redis server. */
  @MockBean private RedisConnectionFactory redisConnectionFactory;

  @MockBean private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

  @Test
  void contextLoads() {
    // Verifies that the entire application context can be initialized,
    // including the JWT security configuration added in Task 2.1.
    // If any bean configuration is broken, this test will fail.
  }
}

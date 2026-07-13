package com.forgemind.infrastructure.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache abstraction. Implementations: Redis, Local (Caffeine).
 * Business services depend ONLY on this interface.
 */
public interface CacheProvider {
  void put(String key, String value, Duration ttl);
  Optional<String> get(String key);
  void evict(String key);
  void evictByPrefix(String prefix);
  boolean exists(String key);
}

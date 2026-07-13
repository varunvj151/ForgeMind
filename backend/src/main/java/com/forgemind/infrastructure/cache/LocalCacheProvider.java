package com.forgemind.infrastructure.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** In-memory fallback cache. Intended for development and CI environments. */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.cache.provider", havingValue = "local")
public class LocalCacheProvider implements CacheProvider {

  private final ConcurrentHashMap<String, ExpiringEntry> store = new ConcurrentHashMap<>();
  private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

  public LocalCacheProvider() {
    cleaner.scheduleAtFixedRate(this::evictExpired, 60, 60, TimeUnit.SECONDS);
  }

  @Override
  public void put(String key, String value, Duration ttl) {
    store.put(key, new ExpiringEntry(value, System.currentTimeMillis() + ttl.toMillis()));
  }

  @Override
  public Optional<String> get(String key) {
    ExpiringEntry e = store.get(key);
    if (e == null || e.isExpired()) {
      store.remove(key);
      return Optional.empty();
    }
    return Optional.of(e.value());
  }

  @Override
  public void evict(String key) { store.remove(key); }

  @Override
  public void evictByPrefix(String prefix) {
    store.keySet().removeIf(k -> k.startsWith(prefix));
  }

  @Override
  public boolean exists(String key) { return get(key).isPresent(); }

  private void evictExpired() {
    store.entrySet().removeIf(e -> e.getValue().isExpired());
  }

  private record ExpiringEntry(String value, long expiresAt) {
    boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
  }
}

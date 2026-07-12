package com.forgemind.modules.organization.ratelimit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-memory sliding-window rate limiter using a token bucket algorithm.
 *
 * <p>Each unique bucket key (e.g. {@code "user:42"} or {@code "org:some-uuid"}) gets its own
 * token bucket. Tokens are refilled at {@code refillRatePerSecond} tokens/second up to
 * {@code capacity}.
 *
 * <p>For production deployments with multiple instances, replace this with a Redis-backed
 * implementation using Lua scripts to ensure atomicity.
 */
@Slf4j
@Component
public class InMemoryRateLimiter {

  private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

  private static final int DEFAULT_CAPACITY = 60;
  private static final int DEFAULT_REFILL_PER_SECOND = 1;

  /**
   * Attempts to consume one token from the bucket identified by the given key.
   *
   * @param bucketKey unique identifier, e.g. "user:42" or "org:uuid"
   * @return true if the request is allowed; false if rate limited
   */
  public boolean tryConsume(String bucketKey) {
    return tryConsume(bucketKey, DEFAULT_CAPACITY, DEFAULT_REFILL_PER_SECOND);
  }

  /**
   * Attempts to consume one token with configurable capacity and refill rate.
   *
   * @param bucketKey          unique bucket identifier
   * @param capacity           maximum number of tokens the bucket can hold
   * @param refillPerSecond    tokens added per second
   * @return true if allowed; false if rate limited
   */
  public boolean tryConsume(String bucketKey, int capacity, int refillPerSecond) {
    TokenBucket bucket = buckets.computeIfAbsent(bucketKey,
        k -> new TokenBucket(capacity, refillPerSecond));
    boolean allowed = bucket.tryConsume();
    if (!allowed) {
      log.warn("Rate limit exceeded for bucket: {}", bucketKey);
    }
    return allowed;
  }

  /** Returns how many tokens remain in the bucket (for headers). */
  public long getRemaining(String bucketKey) {
    TokenBucket bucket = buckets.get(bucketKey);
    return bucket == null ? DEFAULT_CAPACITY : bucket.getTokens();
  }

  // ── Inner Token Bucket ────────────────────────────────────────────────────

  private static class TokenBucket {
    private final int capacity;
    private final int refillPerSecond;
    private final AtomicLong tokens;
    private volatile long lastRefillTimestamp;

    TokenBucket(int capacity, int refillPerSecond) {
      this.capacity = capacity;
      this.refillPerSecond = refillPerSecond;
      this.tokens = new AtomicLong(capacity);
      this.lastRefillTimestamp = System.currentTimeMillis();
    }

    synchronized boolean tryConsume() {
      refill();
      long current = tokens.get();
      if (current > 0) {
        tokens.decrementAndGet();
        return true;
      }
      return false;
    }

    long getTokens() {
      refill();
      return tokens.get();
    }

    private void refill() {
      long now = System.currentTimeMillis();
      long elapsed = now - lastRefillTimestamp;
      long tokensToAdd = (elapsed / 1000L) * refillPerSecond;
      if (tokensToAdd > 0) {
        tokens.set(Math.min(capacity, tokens.get() + tokensToAdd));
        lastRefillTimestamp = now;
      }
    }
  }
}

package com.example.bucketGaurd.core;

import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class TokenBucketEngine {
  private final StringRedisTemplate redisTemplate;
  private final DefaultRedisScript<Long> script;

  // public TokenBucketEngine(StringRedisTemplate redisTemplate,
  // DefaultRedisScript<Long> script) {
  // this.redisTemplate = redisTemplate;
  // this.script = script;
  // }

  public TokenBucketEngine(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;

    // Atomic Token Bucket Lua Script
    String luaScript = "local key = KEYS[1] " +
        "local capacity = tonumber(ARGV[1]) " +
        "local replenish_rate = tonumber(ARGV[2]) " +
        "local now = tonumber(ARGV[3]) " +

        "local bucket = redis.call('hmget', key, 'tokens', 'last_updated') " +
        "local tokens = tonumber(bucket[1]) " +
        "local last_updated = tonumber(bucket[2]) " +

        "if tokens == nil then " +
        "  tokens = capacity " +
        "  last_updated = now " +
        "else " +
        "  local elapsed = math.max(0, now - last_updated) " +
        "  tokens = math.min(capacity, tokens + (elapsed * replenish_rate)) " +
        "end " +

        "if tokens >= 1 then " +
        "  tokens = tokens - 1 " +
        "  redis.call('hset', key, 'tokens', tokens, 'last_updated', now) " +
        "  redis.call('expire', key, math.ceil(capacity / replenish_rate)) " +
        "  return 1 " + // Allowed
        "else " +
        "  return 0 " + // Denied
        "end";

    this.script = new DefaultRedisScript<>(luaScript, Long.class);
  }

  public boolean isAllowed(String key, long capacity, long replenishRate) {
    long nowSeconds = System.currentTimeMillis() / 1000;

    Long result = redisTemplate.execute(
        script,
        Collections.singletonList("rate_limit:" + key),
        String.valueOf(capacity),
        String.valueOf(replenishRate),
        String.valueOf(nowSeconds));

    return result != null && result == 1;
  }

}

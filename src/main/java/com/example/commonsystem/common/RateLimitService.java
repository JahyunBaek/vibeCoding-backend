package com.example.commonsystem.common;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  private static final String PREFIX = "rate:";

  private final StringRedisTemplate redis;

  public RateLimitService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public boolean tryAcquire(String key, int limit, Duration window) {
    String redisKey = PREFIX + key;
    Long count = redis.opsForValue().increment(redisKey);
    if (count != null && count == 1) {
      redis.expire(redisKey, window);
    }
    return count != null && count <= limit;
  }
}

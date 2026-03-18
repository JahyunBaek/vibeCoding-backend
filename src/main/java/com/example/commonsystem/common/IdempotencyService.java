package com.example.commonsystem.common;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 멱등성 키를 Redis에 저장하여 중복 요청을 차단합니다.
 * setIfAbsent(NX) : 키가 없으면 저장(true 반환) → 새 요청 → 처리
 *                    키가 있으면 저장 안 함(false 반환) → 중복 요청 → 차단
 */
@Service
public class IdempotencyService {

  private static final String PREFIX = "idempotency:";
  private static final Duration TTL = Duration.ofHours(24);

  private final StringRedisTemplate redis;

  public IdempotencyService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  /**
   * @return true  → 최초 요청, 처리를 진행해도 됨
   *         false → 중복 요청, 처리를 거부해야 함
   */
  public boolean tryConsume(String key) {
    Boolean isNew = redis.opsForValue().setIfAbsent(PREFIX + key, "1", TTL);
    return Boolean.TRUE.equals(isNew);
  }
}

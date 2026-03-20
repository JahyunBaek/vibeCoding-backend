package com.example.commonsystem.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

  private static final SecureRandom RANDOM = new SecureRandom();
  private final StringRedisTemplate redis;

  public RefreshTokenService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public String issue(long userId, Duration ttl) {
    String token = generateToken();
    String key = key(token);
    redis.opsForValue().set(key, String.valueOf(userId), ttl);
    return token;
  }

  public Long verifyAndGetUserId(String token) {
    if (token == null || token.isBlank()) return null;
    String val = redis.opsForValue().get(key(token));
    if (val == null) return null;
    try {
      return Long.parseLong(val);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public void revoke(String token) {
    if (token == null || token.isBlank()) return;
    redis.delete(key(token));
  }

  private String key(String token) {
    return "refresh:" + token;
  }

  private String generateToken() {
    byte[] bytes = new byte[48];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

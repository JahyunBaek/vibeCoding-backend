package com.example.commonsystem.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

  private final StringRedisTemplate redis;

  public PasswordResetService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  /**
   * 비밀번호 재설정 토큰을 생성하고 Redis에 저장한다.
   * 키: pwd-reset:{token} → userId, TTL 30분.
   */
  public String generateResetToken(long userId) {
    String token = generateToken();
    String key = key(token);
    redis.opsForValue().set(key, String.valueOf(userId), TOKEN_TTL);
    return token;
  }

  /**
   * 토큰을 검증하고 userId를 반환한 뒤 토큰을 삭제한다 (일회용).
   * 유효하지 않으면 null을 반환한다.
   */
  public Long validateAndGetUserId(String token) {
    if (token == null || token.isBlank()) return null;
    String key = key(token);
    String val = redis.opsForValue().get(key);
    if (val == null) return null;
    redis.delete(key);
    try {
      return Long.parseLong(val);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public int getExpiresInMinutes() {
    return (int) TOKEN_TTL.toMinutes();
  }

  private String key(String token) {
    return "pwd-reset:" + token;
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

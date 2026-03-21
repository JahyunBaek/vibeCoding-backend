package com.example.commonsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties props;
  private final Key key;

  public JwtService(JwtProperties props) {
    this.props = props;
    this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * @param tenantId null = SUPER_ADMIN (시스템 레벨 사용자)
   */
  public String createAccessToken(long userId, String username, String roleKey,
      String name, Long orgId, Long tenantId) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.accessTokenMinutes() * 60L);

    return Jwts.builder()
        .subject(username)
        .claim("uid", userId)
        .claim("role", roleKey)
        .claim("name", name)
        .claim("orgId", orgId)
        .claim("tid", tenantId)   // tenantId: null이면 SUPER_ADMIN
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith((javax.crypto.SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}

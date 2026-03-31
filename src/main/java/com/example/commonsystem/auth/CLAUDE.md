# auth — 인증 & 토큰 관리

## 엔드포인트

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/api/auth/login` | 로그인 (Rate Limit: IP당 5회/10분) | 불필요 |
| POST | `/api/auth/refresh` | 토큰 갱신 (쿠키로 refresh token 전송) | 불필요 |
| POST | `/api/auth/logout` | 로그아웃 (refresh token 삭제) | 필요 |
| POST | `/api/auth/reset-password` | 비밀번호 재설정 (토큰 기반) | 불필요 |
| GET | `/api/auth/invitation/{token}` | 초대 토큰 검증 | 불필요 |
| POST | `/api/auth/signup` | 초대 기반 회원가입 | 불필요 |

## 토큰 서비스

### RefreshTokenService

Redis 기반 refresh token 관리.

```java
String token = refreshTokenService.issue(userId, ttlMinutes);  // 발급
Long userId = refreshTokenService.verifyAndGetUserId(token);   // 검증 (null이면 만료)
refreshTokenService.revoke(token);                              // 폐기
```

Redis 키: `refresh:{token}` → userId, TTL = refreshTokenMinutes

### PasswordResetService

1회용 비밀번호 재설정 토큰.

```java
String token = passwordResetService.generateResetToken(userId);     // 생성 (30분 TTL)
Long userId = passwordResetService.validateAndGetUserId(token);     // 검증+삭제 (1회용)
```

Redis 키: `pwd-reset:{token}` → userId

## 인증 흐름

1. `/api/auth/login` → JWT (5분) + refresh cookie (60분, HttpOnly)
2. 모든 요청: `Authorization: Bearer {jwt}` → `JwtAuthFilter` → `SecurityContext`
3. JWT 만료 → 프론트가 `/api/auth/refresh` 호출 → 새 JWT + 새 refresh token (rotation)
4. refresh도 만료 → 로그아웃 처리

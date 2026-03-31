# security — 인증 & JWT

## 핵심 클래스

### UserPrincipal

Spring `UserDetails` 구현체. 인증된 사용자 정보를 담는다.

```java
// 컨트롤러에서 주입
@AuthenticationPrincipal UserPrincipal principal
principal.getUserId()    // Long
principal.getUsername()  // String
principal.getRoleKey()   // "SUPER_ADMIN" | "ADMIN" | "USER"
principal.getTenantId()  // Long (SUPER_ADMIN이면 null)
principal.isSuperAdmin() // boolean
```

### JwtService

JWT 생성/파싱. 직접 JJWT를 호출하지 않고 이것을 사용.

- `createAccessToken(userId, username, roleKey, name, orgId, tenantId)` → JWT 문자열
- `parse(token)` → Claims (`uid`, `role`, `name`, `orgId`, `tid`)

### JwtAuthFilter

`OncePerRequestFilter`. `Authorization: Bearer` 헤더에서 JWT를 추출하고 `SecurityContext`에 `UserPrincipal`을 설정.

### JwtProperties

`app.jwt.*` 설정 바인딩:
- `secret`: 서명 키 (프로덕션: `JWT_SECRET` 환경변수 필수)
- `accessTokenMinutes`: 기본 5분
- `refreshTokenMinutes`: 기본 60분

### SecurityProperties

`app.security.*` 설정 바인딩:
- `refreshCookieName`, `refreshCookiePath`, `refreshCookieSamesite`, `refreshCookieSecure`

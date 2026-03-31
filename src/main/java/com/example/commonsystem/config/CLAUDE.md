# config — Spring 설정 클래스

## 파일 목록

| 클래스 | 역할 |
|--------|------|
| `SecurityConfig` | Spring Security: CSRF off, CORS, stateless, URL별 인가 규칙, JwtAuthFilter 등록 |
| `WebConfig` | `local` 프로필 전용: `/images/**` → 로컬 파일시스템 매핑 |
| `OpenApiConfig` | Swagger UI 설정 (JWT Bearer 인증 스킴 포함) |
| `MessageSourceConfig` | `messages*.properties` MessageSource Bean |
| `PropertiesConfig` | `@EnableConfigurationProperties` (JwtProperties, SecurityProperties, MailProperties) |
| `S3Config` | `dev`/`prod` 프로필 전용: AWS S3Client Bean |
| `MailProperties` | `app.mail.*` 바인딩 (enabled, from, baseUrl) |

## 새 설정 추가 시

- `@ConfigurationProperties` 클래스: 이 패키지에 생성 → `PropertiesConfig`에 등록
- Spring Security 규칙 변경: `SecurityConfig.filterChain()` 수정
- 새 프로필 조건부 빈: `@Profile("xxx")` 적용

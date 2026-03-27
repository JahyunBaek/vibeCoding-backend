# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run locally (requires PostgreSQL + Redis running)
./gradlew bootRun --args='--spring.profiles.active=local'

# Build fat jar
./gradlew clean bootJar

# Run with Docker (includes DB + Redis)
docker compose up --build
```

- Backend: http://localhost:8888
- Swagger UI: http://localhost:8888/swagger-ui/index.html
- Default credentials: `admin` / `Admin1234!`, `user` / `User1234!`

There is no automated test suite configured.

## Architecture

**Spring Boot 3.5.10** with Java 25, PostgreSQL, Redis, and MyBatis (XML-based).

### Layer Structure

Controllers → Services → Mappers (MyBatis) → PostgreSQL

- **Controllers** (`src/main/java/.../`): REST endpoints per domain module
- **Services**: Business logic with `@Transactional`
- **Mappers**: MyBatis `@Mapper` interfaces — SQL is in XML files, not annotations
- **XML Mappers**: `src/main/resources/mappers/{module}/*.xml`
- **Domain objects**: Records/POJOs for entities, `*Command` classes for writes, `*ListRow` classes for paginated reads

### Modules

| Package | Purpose |
|---------|---------|
| `auth/` | Login, refresh, logout, password reset |
| `security/` | JWT (`JwtService`), filter (`JwtAuthFilter`), `UserPrincipal` |
| `user/` | User CRUD + profile (`/api/me`) |
| `role/` | Role management |
| `org/` | Organization hierarchy |
| `menu/` | Menu tree, role-based filtering |
| `board/` | Board + Post + Comment CRUD |
| `file/` | File upload/download (stored in `/app/storage`) |
| `code/` | Common code groups/items (dropdown data, cached in Redis) |
| `tenant/` | Tenant CRUD + provisioning (SUPER_ADMIN only) |
| `dashboard/` | Summary stats |
| `sample/` | 의료 샘플 데이터 (Mock) — 환자/임상시험 API |
| `invitation/` | 사용자 초대 (이메일 기반) |
| `notification/` | 인앱 알림 (댓글, 비밀번호, 역할 변경) |
| `common/` | `ApiResponse<T>`, `PageResponse<T>`, `AppException`, `GlobalExceptionHandler`, `ErrorCode`, `TenantContextHolder`, `RateLimitService`, `CsvExportService`, `I18nService`, `EmailService` |
| `config/` | `SecurityConfig`, `RedisConfig`, `WebMvcConfig`, `OpenApiConfig`, `MessageSourceConfig` |

### Authentication

- **Access token**: JWT (HS256), 5-minute TTL, sent as `Authorization: Bearer {token}`
- **Refresh token**: 48-byte random string, 60-minute TTL, stored in Redis (`refresh:{token}` → `userId`), sent as `HttpOnly` cookie `REFRESH_TOKEN`
- **Token rotation**: each `/api/auth/refresh` call revokes the old token and issues a new pair
- `JwtAuthFilter` extracts claims and populates `UserPrincipal` in the `SecurityContext`
- Authorization uses `@PreAuthorize("hasRole('ADMIN')")` at the method level
- **Rate Limiting**: 로그인 엔드포인트에 IP당 5분/10회 제한 (`RateLimitService`, Redis 기반)
- **JWT Secret 경고**: 기본 시크릿 사용 시 시작 로그에 경고 출력. 프로덕션에서는 `JWT_SECRET` 환경변수 필수

### 비밀번호 재설정

- `PasswordResetService` — Redis에 `pwd-reset:{token}` → userId, 30분 TTL
- `POST /api/auth/reset-password` — 공개 엔드포인트, 토큰+새비밀번호로 재설정
- `POST /api/admin/users/{userId}/reset-token` — 관리자가 리셋 토큰 생성

### Database

- **Flyway** `V1`~`V14` 마이그레이션 — 스키마, 권한, 멀티테넌시, 설정, 감사로그, 전문검색 인덱스, 초대, 알림, 테마, 메뉴순서, 의료샘플
- **MyBatis** `map-underscore-to-camel-case: true` — DB columns use `snake_case`, Java uses `camelCase`
- Common codes are Redis-cached under `codes:{tenantId}:{groupKey}` and invalidated on update
- **전문 검색**: `posts` 테이블에 GIN 인덱스 (`V9`), 현재 제목+내용 ILIKE 검색

### Key Conventions

- All responses wrap with `ApiResponse<T>` (success) or return an `AppException` (caught by `GlobalExceptionHandler`)
- Paginated list endpoints use `PageResponse<T>` with `page`, `size`, `total`
- Admin endpoints are under `/api/admin/**`; user endpoints are `/api/**`; super-admin under `/api/super-admin/**`
- When a new `Board` is created, the service automatically inserts a corresponding `Menu` entry of type `BOARD`
- File metadata is stored in the `files` table; actual files go to the `storage/` directory; associations to posts via `post_files`
- **입력 검증**: 모든 `@RequestBody`에 `@Valid` 적용, DTO에 `@NotBlank`/`@Size` 어노테이션. `GlobalExceptionHandler`가 필드별 에러 메시지 반환
- **CSV 내보내기**: `CsvExportService` (UTF-8 BOM, Excel 호환). `GET /api/admin/users/export`, `GET /api/admin/audit/export`
- **고아 파일 정리**: `OrphanFileCleanupJob` — 매일 새벽 3시, 24시간 이상 미연결 파일 삭제 (`@Scheduled`)
- **Swagger/OpenAPI**: 모든 컨트롤러에 `@Tag`/`@Operation` 적용. UI: `/swagger-ui/index.html`

### 멀티테넌시 (SaaS)

**역할 계층**:
- `SUPER_ADMIN`: `tenant_id = NULL`. 모든 테넌트 데이터 접근. `/api/super-admin/**` 전용 엔드포인트.
- `ADMIN`: 특정 테넌트의 관리자. JWT `tid` 클레임에 tenantId 포함.
- `USER`: 일반 사용자.

**Tenant ID 추출**:
- `TenantContextHolder` (`common/`) — `SecurityContext`의 `UserPrincipal`에서 `tenantId` 추출.
- `currentTenantId()`: `SUPER_ADMIN`이면 `null` 반환 (전체 조회용).
- `isSuperAdmin()`: roleKey가 `"SUPER_ADMIN"`인지 확인.

**모든 테넌트 격리 테이블**: `users`, `orgs`, `menus`, `boards`, `code_groups`, `codes`, `role_actions`, `files`에 `tenant_id` 컬럼 추가.

**글로벌 테이블** (테넌트 격리 없음): `roles`, `screens`, `screen_actions`

**테넌트 프로비저닝**: `TenantService.provisionTenant()` — 새 테넌트 생성 시 메뉴, 게시판, 역할 권한, 공통코드 자동 초기화.

**캐시 키**: Redis 공통코드 캐시는 `codes:{tenantId}:{groupKey}` 형태로 테넌트별 분리.

**테넌트 브랜딩**: `GET /api/tenant/branding` — 인증된 사용자의 테넌트에서 `company_name`, `logo_url` 반환.

**DB 마이그레이션**:
| Version | 내용 |
|---------|------|
| V1 | 초기 스키마 (roles, users, orgs, menus, boards, posts, comments, files, codes) |
| V2 | Screens + Actions + Role-Action 매핑 |
| V3 | ScreenActions 메뉴 추가 |
| V4 | 멀티테넌시: tenant_id 컬럼, tenants 테이블, SUPER_ADMIN |
| V5 | SUPER_ADMIN_TENANTS 화면/액션 |
| V6 | SUPER_ADMIN 비밀번호 해시 수정 |
| V7 | SUPER_ADMIN 시스템 메뉴 |
| V8 | tenant_configs, audit_logs 테이블 |
| V9 | 게시글 전문검색 GIN 인덱스 |
| V10 | 초대(invitations) 테이블 |
| V11 | 알림(notifications) 테이블 |
| V12 | 테넌트 테마 설정 |
| V13 | System 메뉴 순서 변경 (Admin 뒤로) |
| V14 | 의료 샘플 공통코드 + Medical 메뉴 |

### 공통코드 (Common Codes)

공통코드는 드롭다운/콤보박스 등 선택 항목을 관리하는 시스템. Redis 캐시 기반.

**구조**: `code_groups` (그룹) → `codes` (항목). 테넌트별 격리.

**API**:
- `GET /api/common-codes/{groupKey}` — 공개 조회 (캐시)
- `GET/POST/PUT/DELETE /api/admin/codes/groups/**` — 관리자 CRUD

**Redis 캐시**: `codes:{tenantId}:{groupKey}`, TTL 24h, CUD 시 자동 무효화.

**등록된 공통코드 그룹** (V14 기준):

| Group Key | 그룹명 | 항목 예시 |
|-----------|--------|----------|
| `YN` | Y/N | Y, N |
| `PATIENT_STATUS` | 환자 상태 | ACTIVE, DISCHARGED, FOLLOW_UP, INACTIVE |
| `DEPARTMENT` | 진료과 | IM(내과), GS(외과), NR(신경과), CD(심장내과), OG(산부인과), PD(소아과), OS(정형외과), DR(피부과) |
| `BLOOD_TYPE` | 혈액형 | A_POS, A_NEG, B_POS, B_NEG, O_POS, O_NEG, AB_POS, AB_NEG |
| `GENDER` | 성별 | M(남성), F(여성) |
| `TRIAL_PHASE` | 임상시험 단계 | PHASE_1~PHASE_4 |
| `TRIAL_STATUS` | 임상시험 상태 | PLANNED, RECRUITING, ACTIVE, COMPLETED, SUSPENDED |

**새 공통코드 추가 시**:
1. Flyway 마이그레이션에 `code_groups` + `codes` INSERT (tenant_id=1, 0 모두)
2. `TenantService.provisionTenant()`에 `insertCodeGroup()` + `insertCode()` 추가 (신규 테넌트 자동 생성)
3. 프론트에서 `api.commonCodes("GROUP_KEY")`로 조회하여 `<select>` 등에 사용

### 다국어 (i18n)

**MessageSource 기반**: `messages.properties` (기본/ko), `messages_ko.properties`, `messages_en.properties`

**I18nService** (`common/`): `getMessage(code, locale, args...)` — 테넌트 locale에 따라 메시지 반환.

**사용처**: 이메일 템플릿, 알림 메시지, CSV 내보내기 헤더

**테넌트 locale**: `tenant_configs` 테이블의 `locale` 키 (`ko` 또는 `en`). `TenantConfigService.getLocale(tenantId)`로 조회.

### 샘플 데이터 (Medical)

**SampleDataController** (`sample/`): Mock 데이터 반환 (DB 없음).
- `GET /api/sample/patients` — 환자 목록 (page, size, status, department, search)
- `GET /api/sample/trials` — 임상시험 목록 (page, size, phase, status, search)

상태/부서 등의 필터 값은 공통코드에서 조회하여 사용.

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
| `auth/` | Login, refresh, logout |
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
| `common/` | `ApiResponse<T>`, `PageResponse<T>`, `AppException`, `GlobalExceptionHandler`, `ErrorCode`, `TenantContextHolder` |
| `config/` | `SecurityConfig`, `RedisConfig`, `WebMvcConfig`, etc. |

### Authentication

- **Access token**: JWT (HS256), 5-minute TTL, sent as `Authorization: Bearer {token}`
- **Refresh token**: 48-byte random string, 60-minute TTL, stored in Redis (`refresh:{token}` → `userId`), sent as `HttpOnly` cookie `REFRESH_TOKEN`
- **Token rotation**: each `/api/auth/refresh` call revokes the old token and issues a new pair
- `JwtAuthFilter` extracts claims and populates `UserPrincipal` in the `SecurityContext`
- Authorization uses `@PreAuthorize("hasRole('ADMIN')")` at the method level

### Database

- **Flyway** runs `V1__init.sql` on startup — creates all tables and seeds roles, orgs, users, menus, boards, and common codes
- **MyBatis** `map-underscore-to-camel-case: true` — DB columns use `snake_case`, Java uses `camelCase`
- Common codes are Redis-cached under `codes:{groupKey}` and invalidated on update

### Key Conventions

- All responses wrap with `ApiResponse<T>` (success) or return an `AppException` (caught by `GlobalExceptionHandler`)
- Paginated list endpoints use `PageResponse<T>` with `page`, `size`, `total`
- Admin endpoints are under `/api/admin/**`; user endpoints are `/api/**`; super-admin under `/api/super-admin/**`
- When a new `Board` is created, the service automatically inserts a corresponding `Menu` entry of type `BOARD`
- File metadata is stored in the `files` table; actual files go to the `storage/` directory; associations to posts via `post_files`

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

**DB 마이그레이션**: `V4__multi_tenant.sql` — tenants 테이블, 기존 테이블에 tenant_id 컬럼 추가, system tenant(0), default tenant(1), SUPER_ADMIN 시드.

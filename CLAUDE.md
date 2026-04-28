# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew bootRun --args='--spring.profiles.active=local'   # 로컬 실행
./gradlew clean bootJar                                       # 빌드
docker compose up --build                                     # Docker 실행
./gradlew spotlessCheck                                       # 포맷 검사
./gradlew spotlessApply                                       # 포맷 자동 적용
./gradlew compileJava spotlessCheck                           # 컴파일 + 포맷 검사
```

### 검증 스크립트

```bash
bash scripts/check.sh            # 전체 품질 검사 (spotless + compile + build)
bash scripts/check-migration.sh  # Flyway 마이그레이션 파일명/버전/구문 검증
bash scripts/setup.sh            # 개발 환경 초기 설정
```

- Backend: http://localhost:28080
- Swagger UI: http://localhost:28080/swagger-ui/index.html
- 기본 계정: `admin` / `Admin1234!`, `user` / `User1234!`

테스트 프레임워크 없음.

## 기술 스택

Spring Boot 3.5.10, Java 25, PostgreSQL, Redis, MyBatis (XML), Flyway, JJWT, SpringDoc OpenAPI

## 아키텍처

```
Controllers → Services (@Transactional) → Mappers (@Mapper) → PostgreSQL
                                              ↓
                                     XML (src/main/resources/mappers/{module}/*.xml)
```

## 패키지 구조

각 패키지에 상세 규칙이 담긴 `CLAUDE.md`가 있다. 새 코드 작성 시 해당 패키지의 CLAUDE.md를 참고할 것.

```
com.example.commonsystem/
├── common/       # ★ 공통 유틸 — ApiResponse, PageResponse, ErrorCode, AppException,
│                 #   TenantContextHolder, RateLimitService, IdempotencyService,
│                 #   CsvExportService, I18nService, EmailService
├── config/       # Spring 설정 (Security, OpenAPI, Redis, S3 등)
├── security/     # JWT, 인증 필터, UserPrincipal
├── auth/         # 로그인, 토큰 갱신, 비밀번호 재설정
├── permission/   # 화면-액션 권한 (@RequiresAction)
├── tenant/       # 멀티테넌시, 프로비저닝, 브랜딩
├── user/         # 사용자 CRUD + 프로필
├── role/         # 역할 관리
├── org/          # 조직 계층
├── menu/         # 메뉴 트리
├── board/        # 게시판 + 게시글 + 댓글
├── file/         # 파일 업로드/다운로드 (Local/S3)
├── code/         # 공통코드 (Redis 캐시)
├── dashboard/    # 대시보드 통계
├── sample/       # 의료 샘플 Mock 데이터
├── agent/        # AI Agent 채팅 (Mock)
├── approval/     # 공통 결재(전자결재) 시스템 — approval_code 기반
├── invitation/   # 사용자 초대 (이메일)
└── notification/ # 인앱 알림
```

## 핵심 규칙 (모든 패키지 공통)

### 응답

- 모든 엔드포인트: `ApiResponse.ok(data)` 또는 `throw new AppException(ErrorCode.XXX, "메시지")`
- 페이징: `ApiResponse.ok(new PageResponse<>(items, page, size, total))`

### 멀티테넌시

- 테넌트 ID 추출: `TenantContextHolder.currentTenantId()` (직접 JWT 파싱 금지)
- 테넌트 격리 테이블: users, orgs, menus, boards, codes, role_actions, files, audit_logs
- 글로벌 테이블: roles, screens, screen_actions

### DTO 네이밍

| 용도 | 네이밍 | 권장 타입 | 예시 |
|------|--------|----------|------|
| 쓰기 입력 (Service→Mapper) | `*Command` | Lombok class | `PostCreateCommand` |
| 목록 조회 (Mapper→Service) | `*ListRow` | **Lombok class** | `UserListRow` |
| 단건 조회 (Mapper→Service) | `*Detail` | **Lombok class** | `PostDetail` |
| 요청 검증 (Controller 입력) | 컨트롤러 내부 record + `@Valid` | record | `CreatePostRequest` |
| API 응답 (단순 응답) | `*Response` | record 가능 | `LoginResponse` |

### MyBatis 결과 DTO 작성 규칙 ⚠️

**MyBatis 결과로 받는 DTO(`*ListRow`, `*Detail`)는 반드시 Lombok 클래스 사용:**

```java
@Getter
@Setter
@NoArgsConstructor
public static class UserListRow {
    private long userId;
    private String username;
    private boolean activeYn;
    private Instant createdAt;
}
```

**record를 사용하면 안 되는 이유:**
- MyBatis 3.5+가 결과 컬럼을 wrapper type(`Long`, `Boolean`, `Integer`)으로 전달
- record가 primitive(`long`, `boolean`, `int`)로 선언되어 있으면 `NoSuchMethodException` 발생
- nested `<collection>` 매핑 시 setter 기반이라 record는 `IndexOutOfBoundsException` 발생

**XML resultMap도 setter 기반(`<id>/<result>`) 사용**, `<constructor>` 매핑은 피한다.

```xml
<resultMap id="UserListRowMap" type="...UserListRow">
  <id property="userId" column="user_id"/>
  <result property="username" column="username"/>
  <result property="activeYn" column="active_yn"/>
  <result property="createdAt" column="created_at"/>
</resultMap>
```

**record가 적합한 경우:**
- Controller 입력용 Request DTO (Jackson 역직렬화)
- 정적 응답 DTO (Service에서 `new XxxResponse(...)`로 직접 생성)
- 외부 API 응답 매핑 (수동 변환)

### 컨트롤러 작성

```java
@Tag(name = "도메인명", description = "설명")
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    @Operation(summary = "목록 조회")
    @GetMapping
    public ApiResponse<PageResponse<XxxListRow>> list(...) { ... }

    @Operation(summary = "생성")
    @RequiresAction(screen = "XXX", action = "CREATE")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateRequest req,
                                     @AuthenticationPrincipal UserPrincipal principal) { ... }
}
```

### URL 규칙

| Prefix | 대상 |
|--------|------|
| `/api/` | 인증된 사용자 |
| `/api/admin/` | ADMIN + SUPER_ADMIN |
| `/api/super-admin/` | SUPER_ADMIN 전용 |
| `/api/auth/` | 공개 (인증 불필요) |

### DB 마이그레이션 (Flyway)

스키마: `testdb`.

#### ⚠️ 새 마이그레이션 작성 절차 (필수 — 충돌 방지)

여러 PC/브랜치에서 동시 작업 시 **버전 번호 충돌**이 빈발한다. 다음 순서를 반드시 지킬 것:

1. **`git pull` 먼저 실행** — 다른 작업자가 추가한 마이그레이션을 받아온다.
2. **현재 최대 버전 확인**:
   ```bash
   ls src/main/resources/db/migration/ | sort -V | tail -5
   ```
3. 다음 번호로 파일 생성: `V{최대값+1}__{lowercase_snake_설명}.sql`
4. **저장 후 검증 스크립트 실행** — 작업 완료 후 반드시:
   ```bash
   bash scripts/check-migration.sh
   ```
   `Duplicate version` 에러가 나오면 즉시 다음 번호로 rename. 절대 무시하고 push 금지.
5. **충돌 발생 시 (Flyway 기동 실패 또는 push 후 충돌 인지)**:
   - 새로 작성한 파일을 `git mv`로 다음 빈 번호로 rename
   - `build/resources/main/db/migration/` stale 파일도 삭제 (`./gradlew clean` 또는 직접 rm)
   - `[FIX] 마이그레이션 버전 충돌 해결` 커밋

#### 마이그레이션 작성 규칙

- 파일명: `V{N}__{lowercase_snake}.sql` (대문자 V, 더블 언더스코어, 소문자+언더스코어 설명)
- `SET search_path TO testdb;`로 시작
- `CREATE TABLE IF NOT EXISTS`, `INSERT ... ON CONFLICT DO NOTHING` 으로 멱등성 보장
- `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` 사용 (Postgres 9.6+)
- **이미 적용된 마이그레이션은 절대 수정 금지** — 항상 새 버전 추가

### Redis 키 규칙

| 키 패턴 | 용도 |
|---------|------|
| `refresh:{token}` | Refresh token → userId |
| `pwd-reset:{token}` | 비밀번호 재설정 토큰 |
| `rate:{key}` | Rate limit 카운터 |
| `idempotency:{key}` | 멱등성 키 |
| `codes:{tenantId}:{groupKey}` | 공통코드 캐시 |

## 새 도메인 추가 절차

1. 패키지 생성: `com.example.commonsystem.{domain}/controller/`, `service/`, `mapper/`
2. 마이그레이션: `src/main/resources/db/migration/V{N}__xxx.sql` (테이블 생성)
   → **`git pull` + `ls db/migration | sort -V | tail` 로 최대 버전 확인 후 +1**
   → 작업 완료 후 `bash scripts/check-migration.sh` 필수
3. Mapper XML: `src/main/resources/mappers/{domain}/XxxMapper.xml`
4. 응답: `ApiResponse.ok()` / `PageResponse` 사용
5. 테넌트: `TenantContextHolder`로 tenantId 처리
6. 권한: `@RequiresAction` 필요 시 적용
7. Swagger: `@Tag`, `@Operation` 적용
8. 프론트: `src/lib/api/{domain}.ts` + `index.ts` 등록

## 브랜치 전략

- 큰 단위의 신규 기능 개발 시 `feature/xxxx` 브랜치(원격 포함)를 생성하여 개발 및 테스트한다.
- 문제없으면 `dev` 브랜치에 merge 한다.
- `main` 브랜치로의 merge는 **사용자가 직접** 진행한다. (Claude가 main에 merge하지 않는다.)

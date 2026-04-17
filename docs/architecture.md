# System Architecture

## 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────┐
│                     Client (Browser)                     │
│                                                         │
│  React 18 + TypeScript + Vite                           │
│  TanStack Query (서버 상태) + Zustand (클라이언트 상태)     │
│  shadcn/ui + Tailwind CSS                               │
│  i18next (ko/en)                                        │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP (REST API)
                        │ Vite proxy: /api → :28080
┌───────────────────────▼─────────────────────────────────┐
│                  Spring Boot 3.5 (Java 25)               │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐               │
│  │ Security │  │ JWT Auth │  │  Tenant  │               │
│  │  Filter  │→ │  Filter  │→ │ Context  │               │
│  └──────────┘  └──────────┘  └──────────┘               │
│                        │                                 │
│  ┌─────────────────────▼────────────────────────────┐   │
│  │              REST Controllers                     │   │
│  │  auth / user / role / org / menu / board / code   │   │
│  │  sample / agent / notification / audit            │   │
│  │  genomics (samples/panels/variants/reports/pgx)   │   │
│  └─────────────────────┬────────────────────────────┘   │
│                        │                                 │
│  ┌─────────────────────▼────────────────────────────┐   │
│  │              Services (@Transactional)             │   │
│  └─────────────────────┬────────────────────────────┘   │
│                        │                                 │
│  ┌─────────────────────▼────────────────────────────┐   │
│  │         MyBatis Mappers (XML + Interface)          │   │
│  └─────────────────────┬────────────────────────────┘   │
│                        │                                 │
└────────────────────────┼─────────────────────────────────┘
                         │
       ┌─────────────────┼──────────────────┐
       │                 │                  │
┌──────▼──────┐  ┌───────▼──────┐  ┌───────▼──────┐
│ PostgreSQL  │  │    Redis     │  │   AWS S3     │
│  (testdb)   │  │  (캐시/토큰) │  │  (파일저장)   │
└─────────────┘  └──────────────┘  └──────────────┘
```

## 인증/인가 흐름

```
Login → JWT 발급 (accessToken + refreshToken 쿠키)
  → accessToken: 메모리(Zustand) 보관, 요청마다 Bearer 헤더 자동 첨부
  → refreshToken: HttpOnly 쿠키
  → 401 발생 시: single-flight refresh → 성공하면 재시도, 실패하면 로그아웃
```

## 멀티테넌시

```
SUPER_ADMIN (tenantId=null) → 모든 테넌트 접근
ADMIN       (tenantId=N)    → 해당 테넌트만
USER        (tenantId=N)    → 해당 테넌트만

TenantContextHolder가 SecurityContext에서 tenantId 추출
→ 모든 쿼리에 WHERE tenant_id = ? 자동 적용
```

## 모듈 구조

### 핵심 인프라

| 모듈 | 역할 |
|------|------|
| `security` | JWT 발급/검증, 인증 필터, UserPrincipal |
| `auth` | 로그인, 토큰 갱신, 비밀번호 재설정 |
| `permission` | 화면-액션 권한 (@RequiresAction) |
| `tenant` | 멀티테넌시, 프로비저닝, 브랜딩 |
| `common` | ApiResponse, ErrorCode, TenantContextHolder, EmailService |
| `config` | SecurityConfig, S3Config, RedisConfig |

### 비즈니스 도메인

| 모듈 | 역할 |
|------|------|
| `user` | 사용자 CRUD + 프로필 |
| `role` | 역할 관리 |
| `org` | 조직 계층 |
| `menu` | 메뉴 트리 |
| `board` | 게시판 + 게시글 + 댓글 |
| `code` | 공통코드 (Redis 캐시) |
| `file` | 파일 업로드/다운로드 (Local/S3) |
| `notification` | 인앱 알림 |
| `audit` | 감사 로그 |

### 확장 도메인 (예시)

| 모듈 | 역할 |
|------|------|
| `sample` | 샘플 Mock 데이터 (데모용) |
| `agent` | AI Agent 채팅 (Gemini 실연동 + Mock) |

> 도메인별 모듈 추가는 `CLAUDE.md`의 "새 도메인 추가 절차"를 참조.

## 외부 연동

| 서비스 | 용도 | 환경별 설정 |
|--------|------|-------------|
| PostgreSQL | 메인 DB | local: localhost, dev/prod: RDS |
| Redis | 토큰/캐시/Rate Limit | local: localhost, dev/prod: ElastiCache |
| AWS S3 | 파일 저장 | local: 로컬 디스크, dev/prod: S3 |
| AWS Secrets Manager | 민감 정보 관리 | dev/prod only |
| Google Gemini API | AI 분석 | API Key (local: 환경변수, dev/prod: Secrets Manager) |

## DB 마이그레이션 히스토리

| 버전 | 내용 |
|------|------|
| V1 | 초기 스키마 (users, roles, orgs, menus, boards, codes) |
| V2~V13 | 스키마 확장, 인덱스, 시드 데이터 |
| V14 | 샘플 Mock 데이터 |
| V15 | Analysis Agent 메뉴 |

## 코드 품질 도구

| 도구 | 프로젝트 | 명령어 |
|------|---------|--------|
| ESLint | Frontend | `npm run lint` |
| Prettier | Frontend | `npm run format` |
| TypeScript | Frontend | `npm run typecheck` |
| Spotless | Backend | `./gradlew spotlessCheck` |
| 통합 검사 | Frontend | `npm run check` |
| 통합 검사 | Backend | `./gradlew compileJava spotlessCheck` |

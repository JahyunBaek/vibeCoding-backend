# Common System Starter — Backend

## 개요

Spring Boot 3 기반 SaaS 멀티테넌트 백엔드 시스템. 조직, 사용자, 역할, 게시판, 파일 관리 등 업무 시스템에 공통으로 필요한 기능을 제공하는 스타터 프로젝트이다.

**주요 기능:**

- JWT 기반 인증/인가 (Access Token + Refresh Token HttpOnly 쿠키)
- 멀티테넌시: 테넌트별 데이터 격리, 브랜딩, 설정
- 화면-액션 기반 세밀한 권한 제어 (`@RequiresAction`)
- 조직도(트리), 메뉴(트리), 역할, 사용자 CRUD
- 게시판/게시글/댓글 + 파일 첨부 + HTML 에디터 이미지 업로드
- 공통코드 관리 (코드그룹 → 코드)
- 감사 로그 (Audit Log)
- 알림 시스템 (Notification)
- 초대 기반 회원가입
- CSV 내보내기 (사용자, 감사로그)
- 비밀번호 재설정 (Redis 토큰, 30분 TTL, 1회용)
- 이메일 발송 (비밀번호 재설정, 초대)
- 다국어 지원 (한국어/영어)
- Swagger UI API 문서
- Rate Limiting (Redis 기반)
- XSS 방어 (Jsoup)

---

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | Spring Boot | 3.5.10 |
| Language | Java | 25 |
| Build | Gradle | 9.0.0 |
| Database | PostgreSQL | - |
| ORM/SQL | MyBatis | 3.0.4 |
| DB Migration | Flyway | (Spring Boot BOM) |
| Cache/Session | Redis | (Spring Boot BOM) |
| Authentication | JWT (jjwt) | 0.12.6 |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.6.0 |
| File Storage | Local / AWS S3 | - |
| Cloud | Spring Cloud AWS (Secrets Manager, S3) | 3.3.0 |
| AWS SDK | AWS SDK v2 | 2.29.51 |
| Email | Spring Mail (Gmail / AWS SES) | (Spring Boot BOM) |
| SQL Logging | P6Spy | 1.12.1 |
| XSS 방어 | Jsoup | 1.17.2 |
| Utility | Lombok | (Spring Boot BOM) |
| Container | Docker (eclipse-temurin:25-jdk) | - |

---

## 시작하기

### 사전 요구사항

- **Java 25** (Eclipse Temurin 권장)
- **PostgreSQL** (기본: `localhost:5432`, DB명: `appdb`, 스키마: `testdb`)
- **Redis** (기본: `localhost:6379`)

### 로컬 실행

```bash
# 1. 프로젝트 루트(backend/)에서 실행
./gradlew bootRun

# 기본 포트: 28080
# 기본 프로필: local
# Swagger UI: http://localhost:28080/swagger-ui/index.html
```

환경변수로 DB/Redis 접속 정보를 변경할 수 있다:

```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=appdb DB_SCHEMA=testdb \
DB_USER=app DB_PASSWORD=app1234! \
REDIS_HOST=localhost REDIS_PORT=6379 \
./gradlew bootRun
```

### Docker 실행

```bash
# 1. 이미지 빌드
docker build -t common-system-backend .

# 2. 컨테이너 실행
docker run -d \
  --name common-system-backend \
  -p 8888:8888 \
  -e SERVER_PORT=8888 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=appdb \
  -e DB_SCHEMA=testdb \
  -e DB_USER=app \
  -e DB_PASSWORD=app1234! \
  -e REDIS_HOST=host.docker.internal \
  -e REDIS_PORT=6379 \
  -e JWT_SECRET=your-secret-key-at-least-32-bytes \
  common-system-backend
```

> Dockerfile은 multi-stage 빌드를 사용한다. 빌드 단계에서 Gradle 9.0.0을 설치하고 `bootJar`를 생성한 후, 실행 단계에서 JAR만 복사하여 실행한다.

---

## 프로젝트 구조

```
src/main/java/com/example/commonsystem/
├── CommonSystemApplication.java   # 메인 애플리케이션
├── auth/            # 인증 (로그인, 토큰 갱신, 로그아웃)
├── user/            # 사용자 관리 (내 정보, 관리자 CRUD)
├── role/            # 역할 관리
├── org/             # 조직(트리) 관리
├── menu/            # 메뉴(트리) 관리
├── board/           # 게시판, 게시글, 댓글
├── file/            # 파일 업로드/다운로드, 저장소 추상화
├── code/            # 공통코드 (코드그룹 + 코드)
├── tenant/          # 테넌트 관리, 브랜딩, 설정
├── permission/      # 화면-액션 권한 (@RequiresAction, AOP)
├── audit/           # 감사 로그
├── notification/    # 알림 시스템
├── invitation/      # 초대 기반 회원가입
├── sample/          # 샘플 데이터 API
├── dashboard/       # 대시보드 API
├── security/        # Spring Security 설정, JWT 필터
├── config/          # WebMvc, Swagger, Redis, CORS 등 설정
└── common/          # 공통 유틸리티 (ErrorCode, ApiResponse, TenantContextHolder, RateLimitService 등)
```

```
src/main/resources/
├── application.yml          # 공통 설정 (프로필: local)
├── application-local.yml    # 로컬 개발 설정
├── application-dev.yml      # 개발 서버 설정 (AWS Secrets Manager)
├── application-prod.yml     # 운영 서버 설정 (AWS Secrets Manager)
├── messages.properties      # 기본 메시지 (한국어)
├── messages_ko.properties   # 한국어 메시지
├── messages_en.properties   # 영어 메시지
├── db/migration/            # Flyway 마이그레이션 SQL
└── mappers/                 # MyBatis XML 매퍼
```

---

## 프로필별 설정

### local (로컬 개발)

| 항목 | 설정 |
|------|------|
| DB | `localhost:5432/appdb?currentSchema=testdb` |
| Redis | `localhost:6379` |
| JWT | Access 5분, Refresh 60분 |
| 쿠키 | `Secure=false`, `SameSite=Lax` |
| 파일 저장 | 로컬 디스크 (`C:/storage` 기본값) |
| 이메일 | Gmail SMTP (기본 비활성) |
| SQL 로깅 | P6Spy 활성 |
| AWS Secrets Manager | 비활성 |

### dev (개발 서버)

| 항목 | 설정 |
|------|------|
| DB | AWS RDS (Secrets Manager에서 주입) |
| Redis | ElastiCache (Secrets Manager에서 주입) |
| JWT | Access 15분, Refresh 120분 |
| 쿠키 | `Secure=true`, `SameSite=Lax` |
| 파일 저장 | AWS S3 + CloudFront CDN |
| 이메일 | AWS SES (활성) |
| SQL 로깅 | P6Spy 활성 (기본 상속) |
| AWS Secrets Manager | `common-system/dev` 시크릿 사용 |

### prod (운영)

| 항목 | 설정 |
|------|------|
| DB | AWS RDS (Secrets Manager에서 주입) |
| Redis | ElastiCache (Secrets Manager에서 주입) |
| JWT | Access 5분, Refresh 60분 |
| 쿠키 | `Secure=true`, `SameSite=Strict` |
| 파일 저장 | AWS S3 + CloudFront CDN |
| 이메일 | AWS SES (활성) |
| SQL 로깅 | P6Spy **비활성** |
| AWS Secrets Manager | `common-system/prod` 시크릿 사용 |
| AWS 인증 | IAM Role 기반 (Access Key 불필요) |

---

## AWS Secrets Manager 설정 가이드

dev/prod 프로필은 `spring.config.import: aws-secretsmanager:common-system/{profile}`을 통해 AWS Secrets Manager에서 민감 정보를 자동 주입받는다.

### 1단계: AWS Console에서 시크릿 생성

AWS Secrets Manager 콘솔에서 **다른 유형의 시크릿** > **일반 텍스트**로 JSON을 입력한다.

**시크릿 이름:** `common-system/dev` 또는 `common-system/prod`

```json
{
  "db.host":        "dev-db.xxxx.ap-northeast-2.rds.amazonaws.com",
  "db.port":        "5432",
  "db.name":        "appdb",
  "db.schema":      "common_system",
  "db.username":    "app_dev",
  "db.password":    "xxxxxxxx",
  "redis.host":     "dev-redis.xxxx.cache.amazonaws.com",
  "redis.port":     "6379",
  "redis.password": "",
  "jwt.secret":     "your-production-grade-secret-key-at-least-32-bytes",
  "mail.host":      "email-smtp.ap-northeast-2.amazonaws.com",
  "mail.username":  "AKIA...",
  "mail.password":  "xxxxxxxx",
  "s3.bucket":      "common-system-dev-files",
  "cdn.base-url":   "https://dxxxxxx.cloudfront.net"
}
```

### 2단계: JSON 키 설명

| 키 | 설명 |
|----|------|
| `db.host` | PostgreSQL 호스트 (RDS 엔드포인트) |
| `db.port` | PostgreSQL 포트 (기본 5432) |
| `db.name` | 데이터베이스 이름 |
| `db.schema` | 스키마 이름 |
| `db.username` | DB 사용자 |
| `db.password` | DB 비밀번호 |
| `redis.host` | Redis 호스트 (ElastiCache 엔드포인트) |
| `redis.port` | Redis 포트 (기본 6379) |
| `redis.password` | Redis 비밀번호 (없으면 빈 문자열) |
| `jwt.secret` | JWT 서명 키 (최소 32바이트) |
| `mail.host` | SMTP 서버 호스트 (AWS SES 엔드포인트) |
| `mail.username` | SMTP 사용자 (SES SMTP 자격증명) |
| `mail.password` | SMTP 비밀번호 |
| `s3.bucket` | S3 버킷 이름 |
| `cdn.base-url` | CloudFront 배포 URL (이미지 공개 URL 생성에 사용) |

### 3단계: IAM 정책 예시

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "SecretsManagerRead",
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:common-system/*"
    },
    {
      "Sid": "S3FileStorage",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::common-system-*-files",
        "arn:aws:s3:::common-system-*-files/*"
      ]
    }
  ]
}
```

### 4단계: EC2/ECS에서 IAM Role 연결

- **EC2**: 인스턴스에 IAM Role 연결 (인스턴스 프로파일)
- **ECS**: Task Definition에 `taskRoleArn` 지정
- **prod 프로필**: `spring.cloud.aws.credentials` 에 access-key/secret-key를 지정하지 않으면 자동으로 IAM Role을 사용한다
- **dev 프로필**: 필요 시 환경변수 `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`로 명시적 자격증명 가능

---

## 인증 & 보안

### JWT 흐름

1. **로그인** (`POST /api/auth/login`): username/password 검증 후 Access Token + Refresh Token 발급
2. **Access Token**: 응답 body에 포함, 클라이언트 메모리에 보관
3. **Refresh Token**: `REFRESH_TOKEN` HttpOnly 쿠키로 설정 (경로: `/api/auth`)
4. **토큰 갱신** (`POST /api/auth/refresh`): Refresh Token 쿠키로 새 Access Token 발급
5. **로그아웃** (`POST /api/auth/logout`): Refresh Token 쿠키 삭제

### 토큰 유효기간

| 프로필 | Access Token | Refresh Token |
|--------|-------------|---------------|
| local | 5분 | 60분 |
| dev | 15분 | 120분 |
| prod | 5분 | 60분 |

### 쿠키 설정

| 프로필 | Secure | SameSite | Path |
|--------|--------|----------|------|
| local | `false` | `Lax` | `/api/auth` |
| dev | `true` | `Lax` | `/api/auth` |
| prod | `true` | `Strict` | `/api/auth` |

### Rate Limiting

Redis 기반 Rate Limiting 서비스 (`RateLimitService`). 키 + 제한 횟수 + 윈도우 단위로 제한하며, 인증 관련 엔드포인트(로그인 등)에 적용된다.

---

## API 엔드포인트

| 그룹 | 경로 | 설명 |
|------|------|------|
| **Auth** | `/api/auth/**` | 로그인, 토큰 갱신, 로그아웃 |
| **Me** | `/api/me/**` | 내 정보 조회/수정, 비밀번호 변경 |
| **Admin Users** | `/api/admin/users/**` | 사용자 관리 (CRUD, 비밀번호 재설정, CSV 내보내기) |
| **Admin Roles** | `/api/admin/roles/**` | 역할 관리 |
| **Admin Orgs** | `/api/admin/orgs/**` | 조직 관리 (트리) |
| **Admin Menus** | `/api/admin/menus/**` | 메뉴 관리 (트리) |
| **Admin Boards** | `/api/admin/boards/**` | 게시판 관리 |
| **Admin Codes** | `/api/admin/codes/**` | 공통코드 관리 |
| **Admin Audit** | `/api/admin/audit/**` | 감사 로그 조회, CSV 내보내기 |
| **Admin Settings** | `/api/admin/settings/**` | 테넌트 설정 관리 |
| **Admin Permissions** | `/api/admin/permissions/**` | 화면-액션 권한 관리 |
| **Admin Invitations** | `/api/admin/invitations/**` | 초대 관리 |
| **Boards** | `/api/boards/**` | 게시판 목록 조회 |
| **Posts** | `/api/boards/{boardId}/posts/**` | 게시글 CRUD |
| **Comments** | `/api/posts/{postId}/comments/**` | 댓글 CRUD |
| **Files** | `/api/files/**` | 파일 업로드/다운로드 |
| **Common Codes** | `/api/common-codes/**` | 공통코드 조회 (공개) |
| **Menus** | `/api/menus/**` | 내 메뉴 트리 조회 |
| **Permissions** | `/api/permissions/**` | 내 권한 조회 |
| **Notifications** | `/api/notifications/**` | 알림 조회/관리 |
| **Dashboard** | `/api/dashboard/**` | 대시보드 데이터 |
| **Sample** | `/api/sample/**` | 샘플 데이터 API |
| **Tenant Branding** | `/api/tenant/**` | 테넌트 브랜딩 정보 |
| **Super Admin** | `/api/super-admin/tenants/**` | 테넌트 관리 (SUPER_ADMIN 전용) |
| **Swagger UI** | `/swagger-ui/index.html` | API 문서 |

---

## 데이터베이스 마이그레이션

Flyway를 사용하여 스키마를 자동 마이그레이션한다. 마이그레이션 파일은 `src/main/resources/db/migration/`에 위치한다.

| 버전 | 파일 | 설명 |
|------|------|------|
| V1 | `V1__init.sql` | 초기 스키마 (roles, orgs, users, menus, boards, posts, comments, files, common codes 등) |
| V2 | `V2__screen_action_permission.sql` | 화면-액션 권한 테이블 (screens, actions, role_action_permissions) |
| V3 | `V3__add_screen_actions_menu.sql` | 화면 관리 메뉴 추가 |
| V4 | `V4__multi_tenant.sql` | 멀티테넌시 지원 (tenants 테이블, tenant_id 컬럼 추가) |
| V5 | `V5__super_admin_permissions.sql` | SUPER_ADMIN 권한 데이터 |
| V6 | `V6__fix_superadmin_password.sql` | SUPER_ADMIN 비밀번호 수정 |
| V7 | `V7__superadmin_system_menus.sql` | SUPER_ADMIN 전용 시스템 메뉴 |
| V8 | `V8__tenant_config_audit_log.sql` | 테넌트 설정 + 감사 로그 테이블 |
| V9 | `V9__posts_fulltext_index.sql` | 게시글 전문 검색 인덱스 |
| V10 | `V10__invitations.sql` | 초대 테이블 |
| V11 | `V11__notifications.sql` | 알림 테이블 |
| V12 | `V12__tenant_theme_configs.sql` | 테넌트 테마 설정 |
| V13 | `V13__reorder_system_menu.sql` | 시스템 메뉴 순서 재정렬 |
| V14 | `V14__sample_medical_data.sql` | 샘플 의료 데이터 (공통코드: 환자상태, 진료과, 혈액형 등) |

---

## 공통코드

`V14__sample_medical_data.sql`에서 등록되는 샘플 공통코드 그룹:

| 그룹 키 | 그룹명 | 코드 예시 |
|---------|--------|----------|
| `YN` | 예/아니오 | `Y`, `N` |
| `PATIENT_STATUS` | 환자 상태 | `ACTIVE`, `DISCHARGED`, `FOLLOW_UP`, `INACTIVE` |
| `DEPARTMENT` | 진료과 | 내과, 외과, 소아과 등 |
| `BLOOD_TYPE` | 혈액형 | `A`, `B`, `O`, `AB` |
| `GENDER` | 성별 | `M`, `F` |
| `TRIAL_PHASE` | 임상시험 단계 | `PHASE_1`, `PHASE_2`, `PHASE_3`, `PHASE_4` |
| `TRIAL_STATUS` | 임상시험 상태 | `PLANNED`, `RECRUITING`, `ONGOING`, `COMPLETED` |

> 공통코드는 테넌트별로 관리된다 (`tenant_id` 컬럼). 관리자 화면(`/admin/codes`)에서 추가/수정/삭제 가능.

---

## 파일 저장소

파일 저장소는 `FileStorageProvider` 인터페이스로 추상화되어 있으며, 프로필에 따라 구현체가 자동 선택된다.

```
┌─────────────────────────────────┐
│        FileService              │
│  (업로드/다운로드 비즈니스 로직)    │
└──────────┬──────────────────────┘
           │ FileStorageProvider
           ├──────────────────────────┐
           │                          │
┌──────────▼──────────┐   ┌──────────▼──────────┐
│ LocalFileStorage    │   │ S3FileStorage       │
│ Provider            │   │ Provider            │
│                     │   │                     │
│ - 로컬 디스크 저장   │   │ - AWS S3 업로드      │
│ - 경로: C:/storage  │   │ - CDN URL 반환       │
│ - local 프로필      │   │ - dev/prod 프로필    │
└─────────────────────┘   └─────────────────────┘
```

### 인터페이스 메서드

| 메서드 | 설명 |
|--------|------|
| `store(key, stream, size, contentType)` | 파일 저장 |
| `load(key)` | 파일 다운로드 (Spring Resource 반환) |
| `delete(key)` | 파일 삭제 |
| `resolvePublicUrl(key)` | 공개 URL 반환 (Local: 상대경로, S3: CDN URL) |

### 저장 경로 규칙

파일은 날짜 기반 경로로 저장된다: `{YYYY}/{MM}/{DD}/{UUID}_{원본파일명}`

---

## 다국어 (i18n)

Spring `MessageSource` 기반으로 다국어를 지원한다.

| 파일 | 용도 |
|------|------|
| `messages.properties` | 기본 메시지 (한국어) |
| `messages_ko.properties` | 한국어 메시지 |
| `messages_en.properties` | 영어 메시지 |

### 지원 메시지 카테고리

- **이메일 템플릿**: 비밀번호 재설정, 초대 이메일 제목/본문
- **CSV 내보내기**: 사용자 목록, 감사 로그 헤더
- **알림**: 댓글, 비밀번호 변경, 역할 변경 알림

> 테넌트별 locale 설정을 통해 테넌트마다 다른 언어를 사용할 수 있다.

---

## 이메일

### 템플릿

| 템플릿 | 용도 | 트리거 |
|--------|------|--------|
| 비밀번호 재설정 | 비밀번호 재설정 링크 발송 | 관리자가 사용자 비밀번호 재설정 시 |
| 초대 | 조직 초대 이메일 | 관리자가 사용자 초대 시 |

### SMTP 설정

| 프로필 | SMTP 서버 | 비고 |
|--------|----------|------|
| local | `smtp.gmail.com:587` | Gmail SMTP (기본 비활성, `MAIL_ENABLED=true`로 활성화) |
| dev | `email-smtp.ap-northeast-2.amazonaws.com:587` | AWS SES (활성) |
| prod | `email-smtp.ap-northeast-2.amazonaws.com:587` | AWS SES (활성) |

---

## 환경변수 참조

### local 프로필

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `SERVER_PORT` | `28080` | 서버 포트 |
| `DB_HOST` | `localhost` | PostgreSQL 호스트 |
| `DB_PORT` | `5432` | PostgreSQL 포트 |
| `DB_NAME` | `appdb` | 데이터베이스 이름 |
| `DB_SCHEMA` | `testdb` | 스키마 이름 |
| `DB_USER` | `app` | DB 사용자 |
| `DB_PASSWORD` | `app1234!` | DB 비밀번호 |
| `REDIS_HOST` | `localhost` | Redis 호스트 |
| `REDIS_PORT` | `6379` | Redis 포트 |
| `REDIS_PASSWORD` | (빈값) | Redis 비밀번호 |
| `JWT_SECRET` | `change-me-super-secret-key-at-least-32-bytes` | JWT 서명 키 |
| `FILE_STORAGE_PATH` | `C:/storage` | 로컬 파일 저장 경로 |
| `MAIL_ENABLED` | `false` | 이메일 발송 활성화 |
| `MAIL_USERNAME` | (빈값) | SMTP 사용자 |
| `MAIL_PASSWORD` | (빈값) | SMTP 비밀번호 |
| `MAIL_FROM` | `noreply@common-system.com` | 발신자 이메일 |
| `APP_BASE_URL` | `http://localhost:5173` | 앱 기본 URL (이메일 링크 생성용) |

### dev/prod 프로필

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `AWS_REGION` | `ap-northeast-2` | AWS 리전 |
| `AWS_ACCESS_KEY` | (빈값) | AWS Access Key (dev에서 사용, prod는 IAM Role 권장) |
| `AWS_SECRET_KEY` | (빈값) | AWS Secret Key (dev에서 사용, prod는 IAM Role 권장) |
| `MAIL_FROM` | `noreply@common-system.com` | 발신자 이메일 |
| `APP_BASE_URL` | `https://dev.common-system.com` (dev) / 필수 (prod) | 앱 기본 URL |

> dev/prod 프로필에서는 DB, Redis, JWT, S3 등 대부분의 민감 정보가 AWS Secrets Manager에서 자동 주입되므로 별도 환경변수 설정이 불필요하다.

# Phase 6: 규제/보안 — 동의서, 비식별화, 감사 로그

## 개요

SaaS 유전체 분석 플랫폼의 규제 대응 기능을 구현한다.
개인정보보호법/HIPAA 요구사항에 맞는 동의서 관리, 비식별화 내보내기, 유전체 데이터 접근 감사 로그를 포함한다.

## DB 스키마 (V19)

### genomic_consents — 동의서

| 컬럼 | 설명 |
|------|------|
| consent_type | GENETIC_TEST, RESEARCH_USE, DATA_SHARING, BIOBANK |
| status | PENDING → SIGNED → REVOKED / EXPIRED |
| signed_at / signed_by_name / witness_name | 서명 정보 |
| expires_at | 만료일 |

**워크플로우:** `PENDING → (sign) → SIGNED → (revoke) → REVOKED`

### genomic_audit_logs — 유전체 접근 감사

| 컬럼 | 설명 |
|------|------|
| action | VIEW_SAMPLE, VIEW_VARIANT, VIEW_REPORT, EXPORT_DATA, UPLOAD_VCF, GENERATE_REPORT, AI_INTERPRET |
| resource_type | SAMPLE, VARIANT, REPORT, PGX, EXPORT |
| ip_address | 접근 IP |

## API

### 동의서 `/api/genomics/consents`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 (patientId, status 필터) |
| GET | `/{id}` | 상세 |
| POST | `/` | 생성 |
| PATCH | `/{id}/sign` | 서명 (signedByName, witnessName) |
| PATCH | `/{id}/revoke` | 철회 |
| DELETE | `/{id}` | 삭제 |

### 비식별화 `/api/genomics/export`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/deidentify/{sampleId}` | 비식별화 CSV 다운로드 |

**비식별화 규칙:**
- sampleNo → 랜덤 UUID (ANON-XXXXXXXX)
- patientId 완전 제거
- 유전체 좌표 + 어노테이션만 포함
- 다운로드 시 감사 로그 자동 기록 (EXPORT_DATA)

### 감사 로그 `/api/genomics/audit`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 조회 (action, resourceType 필터) |

## 테넌트 격리

모든 테이블에 `tenant_id` 포함. 동의서/감사 로그 모두 테넌트별 격리.

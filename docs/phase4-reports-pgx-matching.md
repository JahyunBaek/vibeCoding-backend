# Phase 4: 보고서 생성, 약물유전체(PGx), 임상시험 매칭

## 개요

AI 분석 결과를 보고서로 저장하고, 검출 변이 기반의 약물유전체(PGx) 매칭,
임상시험 자동 추천 기능을 제공한다.

## DB 스키마 (V17)

### genomic_reports — 분석 보고서

| 컬럼 | 타입 | 설명 |
|------|------|------|
| report_id | BIGSERIAL PK | |
| sample_id | BIGINT FK | 대상 샘플 |
| title | VARCHAR(200) | 보고서 제목 (자동 생성) |
| summary | TEXT | AI 생성 분석 요약 |
| variant_count | INT | 총 변이 수 |
| pathogenic_count | INT | Pathogenic/Likely Pathogenic 수 |
| status | VARCHAR(20) | DRAFT → FINAL |

### pgx_mappings — 약물유전체 매핑

| 컬럼 | 타입 | 설명 |
|------|------|------|
| pgx_id | BIGSERIAL PK | |
| gene_symbol | VARCHAR(30) | CYP2D6, CYP2C19 등 |
| variant_name | VARCHAR(100) | *2, *3, rs1234 등 |
| drug_name | VARCHAR(100) | Tamoxifen, Warfarin 등 |
| effect | VARCHAR(30) | POOR_METABOLIZER, INTERMEDIATE, NORMAL, RAPID |
| recommendation | TEXT | 처방 권고 |
| evidence_level | VARCHAR(10) | 1A, 1B, 2A, 2B, 3, 4 |
| source | VARCHAR(50) | CPIC, DPWG, FDA |

## API 엔드포인트

### 보고서 `/api/genomics/reports`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 (page, size, sampleId) |
| GET | `/{reportId}` | 상세 (AI 요약 포함) |
| POST | `/generate/{sampleId}` | AI 요약 포함 보고서 자동 생성 |
| PATCH | `/{reportId}/status?status=FINAL` | 상태 변경 (확정) |
| DELETE | `/{reportId}` | 삭제 |

**보고서 생성 플로우:**
1. sampleId로 변이 전체 조회
2. GenomicsAiService.summarizeSample() 호출
3. 변이 수, Pathogenic 수 계산
4. genomic_reports 테이블에 INSERT

### PGx `/api/genomics/pgx`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | PGx 매핑 DB 목록 (page, size, search) |
| GET | `/match/{sampleId}` | 샘플 변이 기반 PGx 매칭 |

**PGx 매칭 로직:**
```sql
SELECT p.*, v.variant_id, v.acmg_class
FROM pgx_mappings p
JOIN variants v ON v.gene_symbol = p.gene_symbol
                AND v.sample_id = #{sampleId}
```
샘플의 변이 유전자가 PGx DB의 유전자와 일치하면 약물-유전자 상호작용 정보를 반환한다.

## 시드 데이터 (PGx)

CPIC/DPWG 기반 12개 매핑 데이터 포함:

| 유전자 | 변이 | 약물 | 효과 | 근거 |
|--------|------|------|------|------|
| CYP2D6 | *4 | Tamoxifen | Poor Metabolizer | CPIC 1A |
| CYP2D6 | *4 | Codeine | Poor Metabolizer | CPIC 1A |
| CYP2C19 | *2 | Clopidogrel | Poor Metabolizer | CPIC 1A |
| CYP2C9 | *3 | Warfarin | Poor Metabolizer | CPIC 1A |
| DPYD | *2A | Fluorouracil | Poor Metabolizer | CPIC 1A |
| TPMT | *3A | Azathioprine | Poor Metabolizer | CPIC 1A |
| HLA-B | *5801 | Allopurinol | Poor Metabolizer | CPIC 1A |
| ... | | | | |

## 메뉴 추가

```
Genomics (GROUP)
├── Samples   → /genomics/samples
├── Panels    → /genomics/panels
├── Variants  → /genomics/variants
├── Reports   → /genomics/reports    ← NEW
└── PGx       → /genomics/pgx       ← NEW
```

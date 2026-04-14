# Phase 1: 유전체 분석 플랫폼 기반 데이터 모델

## 개요

유전체 분석 플랫폼의 기반 데이터 모델을 구축한다.
환자-샘플-패널-변이의 핵심 엔티티와 CRUD API, 워크플로우 상태 관리를 포함한다.

## DB 스키마 (V16)

### genomic_samples — 유전체 샘플

| 컬럼 | 타입 | 설명 |
|------|------|------|
| sample_id | BIGSERIAL PK | |
| tenant_id | BIGINT NOT NULL | 멀티테넌시 |
| patient_id | BIGINT NOT NULL | 환자 FK |
| sample_no | VARCHAR(30) UNIQUE | 자동채번 (GS-2026-0001) |
| sample_type | VARCHAR(20) | BLOOD, TISSUE, SALIVA, OTHER |
| panel_id | BIGINT nullable | 분석 패널 FK |
| status | VARCHAR(20) | 워크플로우 상태 |
| received_date | DATE | 접수일 |
| completed_date | DATE nullable | 완료일 (COMPLETED/REPORTED 시 자동) |
| note | TEXT | 비고 |

**워크플로우:**
```
RECEIVED → EXTRACTED → SEQUENCING → ANALYZING → COMPLETED → REPORTED
```

### gene_panels — 유전자 패널

| 컬럼 | 타입 | 설명 |
|------|------|------|
| panel_id | BIGSERIAL PK | |
| panel_code | VARCHAR(30) UNIQUE | e.g. ONCO-50, RARE-200 |
| name | VARCHAR(100) | 패널명 |
| category | VARCHAR(30) | TARGETED, WES, WGS |
| gene_count | INT | 자동 계산 |

### panel_genes — 패널-유전자 매핑

| 컬럼 | 타입 | 설명 |
|------|------|------|
| panel_gene_id | BIGSERIAL PK | |
| panel_id | BIGINT FK | CASCADE DELETE |
| gene_symbol | VARCHAR(30) | e.g. BRCA1, TP53 |
| chromosome | VARCHAR(5) | e.g. chr17 |

### variants — 변이 데이터

| 컬럼 | 타입 | 설명 |
|------|------|------|
| variant_id | BIGSERIAL PK | |
| sample_id | BIGINT FK | CASCADE DELETE |
| gene_symbol | VARCHAR(30) | 유전자 |
| chromosome / position | VARCHAR(5) / BIGINT | 게놈 좌표 |
| ref_allele / alt_allele | VARCHAR(500) | REF/ALT |
| variant_type | VARCHAR(10) | SNV, INDEL, CNV, SV |
| acmg_class | VARCHAR(30) | PATHOGENIC ~ BENIGN |
| clinvar_id / gnomad_af / cosmic_id | | 외부 DB 참조 |

## API 엔드포인트

### 샘플 관리 `/api/genomics/samples`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 조회 (page, size, status, search) |
| GET | `/{sampleId}` | 상세 조회 (변이 수 포함) |
| POST | `/` | 등록 (sample_no 자동채번) |
| PUT | `/{sampleId}` | 수정 |
| PATCH | `/{sampleId}/status` | 상태 변경 |
| DELETE | `/{sampleId}` | 삭제 |

### 패널 관리 `/api/genomics/panels`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 조회 (page, size, search) |
| GET | `/active` | 활성 패널 (드롭다운용) |
| GET | `/{panelId}` | 상세 (유전자 목록 포함) |
| POST | `/` | 생성 (유전자 목록 포함) |
| PUT | `/{panelId}` | 수정 (유전자 교체) |
| DELETE | `/{panelId}` | 삭제 (CASCADE) |

### 변이 조회 `/api/genomics/variants`

| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 목록 (다중 필터: gene, type, impact, acmg, gnomadAf, sample) |
| GET | `/{variantId}` | 상세 조회 |

## 패키지 구조

```
genomics/
├── controller/
│   ├── SampleController.java      # @RequiredArgsConstructor
│   ├── PanelController.java
│   └── VariantController.java
├── domain/
│   ├── GenomicSample.java          # record
│   ├── GenePanel.java              # record
│   └── Variant.java                # record
├── dto/
│   ├── SampleDtos.java             # ListRow(record), Detail(record), CreateCommand(Lombok), UpdateCommand(Lombok)
│   ├── PanelDtos.java              # Detail은 Lombok class (MyBatis collection 매핑)
│   └── VariantDtos.java            # ListRow, Detail, Filter (모두 record)
├── mapper/
│   ├── SampleMapper.java
│   ├── PanelMapper.java
│   └── VariantMapper.java
└── service/
    ├── SampleService.java
    ├── PanelService.java
    └── VariantService.java
```

## 공통코드 등록

| Group Key | 코드 | 용도 |
|-----------|------|------|
| SAMPLE_TYPE | BLOOD, TISSUE, SALIVA, OTHER | 검체 유형 |
| SAMPLE_STATUS | RECEIVED ~ REPORTED | 샘플 상태 |
| VARIANT_TYPE | SNV, INDEL, CNV, SV | 변이 유형 |
| ACMG_CLASS | PATHOGENIC ~ BENIGN | ACMG 5단계 |
| PANEL_CATEGORY | TARGETED, WES, WGS | 패널 카테고리 |

## 메뉴 구조

```
Genomics (GROUP, icon: dna)
├── Samples  → /genomics/samples  (icon: test-tubes)
├── Panels   → /genomics/panels   (icon: layout-list)
└── Variants → /genomics/variants (icon: scan-search)
```

역할: ADMIN, USER 모두 접근 가능

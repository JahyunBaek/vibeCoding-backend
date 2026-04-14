# Phase 2: VCF 파일 업로드/파싱 및 변이 상세

## 개요

VCF(Variant Call Format) 파일을 업로드하여 변이 데이터를 자동 파싱 및 DB 적재하고,
변이 상세 조회 및 외부 데이터베이스 연동 기능을 제공한다.

## VCF 파싱 엔진

### 지원 포맷
- VCF 4.x 표준 (.vcf)
- 탭 구분 8+컬럼 (CHROM, POS, ID, REF, ALT, QUAL, FILTER, INFO, [FORMAT, SAMPLE])

### 파싱 항목

| 소스 | 추출 데이터 |
|------|-------------|
| 기본 컬럼 | chromosome, position, ref/alt allele, quality |
| INFO 필드 | DP(read depth), AF(allele frequency), SVTYPE |
| INFO.ANN (SnpEff) | gene_symbol, consequence, impact, hgvsC, hgvsP |
| INFO.CLNSIG | ClinVar significance → ACMG 자동 매핑 |
| INFO.CLNID | ClinVar accession ID |
| INFO.gnomAD_AF | gnomAD allele frequency |
| INFO.COSMIC_ID | COSMIC ID |
| FORMAT/GT | 접합성 (HET, HOM, HEMI) |

### 변이 유형 추론 로직

```
SVTYPE in INFO     → CNV (DEL/DUP/CNV) 또는 SV
REF=1bp, ALT=1bp   → SNV
그 외               → INDEL
```

### CLNSIG → ACMG 매핑

```
"pathogenic" + "likely" → LIKELY_PATHOGENIC
"pathogenic"            → PATHOGENIC
"benign" + "likely"     → LIKELY_BENIGN
"benign"                → BENIGN
"uncertain" / "vus"     → VUS
```

## API

### VCF 업로드

```
POST /api/genomics/samples/{sampleId}/vcf
Content-Type: multipart/form-data
```

**Request:** `file` (MultipartFile, .vcf 또는 .vcf.gz)

**Response:**
```json
{
  "success": true,
  "data": {
    "sampleId": 1,
    "variantCount": 1523
  }
}
```

**처리 흐름:**
1. 파일 확장자 검증 (.vcf, .vcf.gz)
2. 해당 샘플의 기존 변이 전체 삭제 (재업로드 지원)
3. VCF 파싱 → Variant 리스트 생성
4. 500건씩 배치 INSERT
5. 샘플 상태를 ANALYZING으로 변경

## 서비스 클래스

### VcfParserService

VCF 파일 InputStream을 파싱하여 `List<Variant>` 반환.
- 헤더(`#`)와 빈 줄 스킵
- 파싱 실패한 개별 라인은 WARN 로그 후 스킵 (전체 중단하지 않음)

### VcfUploadService

MultipartFile 수신 → VcfParserService 호출 → 배치 적재.
- `@Transactional`: 삭제 + INSERT가 원자적으로 처리됨
- BATCH_SIZE = 500

## MyBatis 추가 쿼리

| ID | 설명 |
|----|------|
| `insertBatch` | `<foreach>` 기반 다건 INSERT |
| `deleteBySampleId` | 샘플별 변이 전체 삭제 |
| `countBySample` | 샘플별 변이 수 조회 |

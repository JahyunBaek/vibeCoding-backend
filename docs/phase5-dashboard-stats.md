# Phase 5: 시각화 — 통계 API

## 개요

변이 분포 통계를 제공하는 API를 구현한다.
전체 또는 특정 샘플 기준으로 다양한 분류별 집계를 반환한다.

## API

### `GET /api/genomics/stats?sampleId={optional}`

**응답:**
```json
{
  "totalVariants": 1523,
  "totalSamples": 12,
  "byChromosome": [{ "label": "chr1", "count": 120 }, ...],
  "byVariantType": [{ "label": "SNV", "count": 1200 }, ...],
  "byImpact": [{ "label": "HIGH", "count": 45 }, ...],
  "byAcmgClass": [{ "label": "PATHOGENIC", "count": 12 }, ...],
  "topGenes": [{ "label": "TP53", "count": 8 }, ...]
}
```

## 통계 항목

| 항목 | SQL | 정렬 |
|------|-----|------|
| byChromosome | GROUP BY chromosome | 염색체 자연 순서 (1,2,...22,X,Y) |
| byVariantType | GROUP BY variant_type | count DESC |
| byImpact | GROUP BY impact | HIGH → MODERATE → LOW → MODIFIER |
| byAcmgClass | GROUP BY acmg_class | PATHOGENIC → ... → BENIGN |
| topGenes | GROUP BY gene_symbol | count DESC, LIMIT 15 |

## 메뉴 (V18)

```
Genomics
├── Dashboard → /genomics/dashboard  (icon: bar-chart-3)  ← NEW
├── Samples
├── Panels
├── Variants
├── Reports
├── PGx
└── Browser  → /genomics/browser  (icon: dna)  ← NEW
```

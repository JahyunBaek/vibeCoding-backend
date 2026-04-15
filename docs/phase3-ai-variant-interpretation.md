# Phase 3: AI 기반 변이 해석 및 샘플 요약

## 개요

Gemini AI를 활용하여 개별 변이의 임상적 의미를 해석하고,
샘플 전체 변이를 종합 분석하는 기능을 제공한다.

## API 엔드포인트

### `/api/genomics/ai`

| Method | Path | 설명 |
|--------|------|------|
| POST | `/interpret/{variantId}` | 단일 변이 AI 해석 |
| POST | `/summarize/{sampleId}` | 샘플 전체 변이 AI 요약 |

### 응답 형식

```json
{
  "success": true,
  "data": {
    "interpretation": "## 임상적 의미\n\n이 변이는..."
  }
}
```

## GenomicsAiService

### 변이 해석 프롬프트

AI에게 다음 정보를 제공하고 분석을 요청한다:
- 유전자, 위치, REF/ALT, HGVS 표기
- 영향도, ACMG 분류, 접합성
- gnomAD AF, ClinVar, COSMIC

**분석 요청 항목:**
1. 임상적 의미
2. ACMG 분류 근거
3. 관련 질환 및 유전 패턴
4. 치료 옵션 / 약물유전체 정보
5. 참고 문헌 및 데이터베이스

### 샘플 요약 프롬프트

AI에게 샘플 통계와 주요 변이 목록(Pathogenic + HIGH impact, 최대 20개)을 제공하고:
1. 변이 프로필 요약
2. 주요 소견(Key Findings)
3. VUS 변이 언급
4. 권장 후속 조치

### Gemini 미연동 시

API 키가 없으면 Mock 응답을 반환한다:
- 실제 분석은 제공하지 않고 안내 메시지만 표시
- 프론트엔드 UI 테스트 가능

### GeminiChatProvider 확장

`buildSystemPrompt`에 `genomics` 케이스 추가:
```
"당신은 임상유전체학(Clinical Genomics) 전문 AI입니다.
 ACMG 가이드라인, ClinVar, gnomAD, COSMIC 등의 지식을 활용합니다."
```

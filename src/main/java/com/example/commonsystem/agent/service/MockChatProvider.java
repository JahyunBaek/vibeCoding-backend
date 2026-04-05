package com.example.commonsystem.agent.service;

import org.springframework.stereotype.Component;

/**
 * Mock 채팅 프로바이더. API 키가 없는 프로바이더에 대해 샘플 응답을 반환한다.
 * 프로바이더별로 별도 인스턴스를 만들지 않고, fallback으로 동작한다.
 */
@Component
public class MockChatProvider {

    public String chat(String providerName, String dataset, String message) {
        String datasetName = switch (dataset) {
            case "patients" -> "환자";
            case "trials" -> "임상시험";
            default -> dataset;
        };

        String lowerMsg = message.toLowerCase();

        if ("patients".equals(dataset)) {
            if (containsAny(lowerMsg, "통계", "요약", "summary")) {
                return """
                    [%s Mock 응답] %s 데이터 요약 분석:

                    📊 **전체 환자 수**: 20명
                    - 활성(ACTIVE): 12명 (60%%)
                    - 퇴원(DISCHARGED): 3명 (15%%)
                    - 추적관찰(FOLLOW_UP): 3명 (15%%)
                    - 비활성(INACTIVE): 2명 (10%%)

                    👥 **성별 분포**: 남성 10명, 여성 10명
                    🏥 **가장 많은 진료과**: 내과(IM) 5명

                    > ⚠️ 이 응답은 Mock 데이터입니다. 실제 AI 연동 시 더 정확한 분석이 제공됩니다.
                    """.formatted(providerName, datasetName);
            }
            if (containsAny(lowerMsg, "혈액형", "blood")) {
                return """
                    [%s Mock 응답] 혈액형 분석:

                    🩸 **혈액형 분포**:
                    - A+ : 5명 (25%%)
                    - B+ : 4명 (20%%)
                    - O+ : 4명 (20%%)
                    - AB+ : 3명 (15%%)
                    - A- : 1명 (5%%)
                    - B- : 1명 (5%%)
                    - O- : 1명 (5%%)
                    - AB- : 1명 (5%%)

                    Rh+ 비율: 80%%, Rh- 비율: 20%%

                    > ⚠️ Mock 응답입니다.
                    """.formatted(providerName);
            }
        }

        if ("trials".equals(dataset)) {
            if (containsAny(lowerMsg, "통계", "요약", "summary")) {
                return """
                    [%s Mock 응답] %s 데이터 요약 분석:

                    📊 **전체 임상시험 수**: 12건
                    - 진행중(ACTIVE): 5건
                    - 모집중(RECRUITING): 2건
                    - 완료(COMPLETED): 2건
                    - 계획(PLANNED): 2건
                    - 중단(SUSPENDED): 1건

                    💊 **단계별 분포**:
                    - Phase 1: 3건 | Phase 2: 4건
                    - Phase 3: 3건 | Phase 4: 1건

                    📈 **전체 등록률**: 78.8%% (2,287/2,739명)

                    > ⚠️ 이 응답은 Mock 데이터입니다.
                    """.formatted(providerName, datasetName);
            }
            if (containsAny(lowerMsg, "등록", "enroll")) {
                return """
                    [%s Mock 응답] 등록 현황 분석:

                    📈 **등록률 상위 시험**:
                    1. CT-2024-005 (COPD 흡입제): 1000/1000명 (100%%)
                    2. CT-2024-012 (아토피 피부염): 90/90명 (100%%)
                    3. CT-2024-010 (전이성 폐암): 412/450명 (91.6%%)

                    📉 **등록률 하위 시험**:
                    1. CT-2024-004 (알츠하이머): 0/80명 (0%% - 계획단계)
                    2. CT-2024-011 (파킨슨병): 0/15명 (0%% - 계획단계)
                    3. CT-2024-007 (소아 백혈병): 22/60명 (36.7%%)

                    > ⚠️ Mock 응답입니다.
                    """.formatted(providerName);
            }
        }

        return """
            [%s Mock 응답]

            "%s" 에 대해 %s 데이터를 기반으로 분석하겠습니다.

            현재는 Mock 모드로 동작 중이며, 실제 AI Agent 연동 시 다음 기능이 제공됩니다:
            - 📊 데이터 통계 분석
            - 🔍 패턴 및 이상치 탐지
            - 📈 트렌드 분석
            - 💡 인사이트 및 추천

            💡 **사용 팁**: "통계", "요약", "혈액형", "등록현황" 등의 키워드로 질문해 보세요.

            > ⚠️ 이 응답은 Mock 데이터입니다. 실제 AI API 키 설정 후 정확한 분석이 제공됩니다.
            """.formatted(providerName, message, datasetName);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}

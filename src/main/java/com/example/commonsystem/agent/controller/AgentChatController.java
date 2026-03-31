package com.example.commonsystem.agent.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Agent Chat", description = "AI Agent 채팅 (Mock)")
@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    // ── 사용 가능한 AI Agent 목록 ──
    @Operation(summary = "Agent 목록 조회")
    @GetMapping("/providers")
    public ApiResponse<List<Map<String, Object>>> providers() {
        List<Map<String, Object>> list = List.of(
            Map.of("id", "chatgpt", "name", "ChatGPT", "model", "gpt-4o",
                    "description", "OpenAI의 대화형 AI", "icon", "openai"),
            Map.of("id", "gemini", "name", "Gemini", "model", "gemini-2.0-flash",
                    "description", "Google의 멀티모달 AI", "icon", "google"),
            Map.of("id", "claude", "name", "Claude", "model", "claude-sonnet-4-20250514",
                    "description", "Anthropic의 안전한 AI", "icon", "anthropic")
        );
        return ApiResponse.ok(list);
    }

    // ── 분석 가능한 샘플 데이터셋 목록 ──
    @Operation(summary = "분석 가능 데이터셋 목록")
    @GetMapping("/datasets")
    public ApiResponse<List<Map<String, Object>>> datasets() {
        List<Map<String, Object>> list = List.of(
            Map.of("id", "patients", "name", "환자 데이터",
                    "description", "20명의 환자 정보 (상태, 진료과, 혈액형 등)",
                    "recordCount", 20),
            Map.of("id", "trials", "name", "임상시험 데이터",
                    "description", "12건의 임상시험 정보 (단계, 상태, 등록현황 등)",
                    "recordCount", 12)
        );
        return ApiResponse.ok(list);
    }

    // ── 채팅 메시지 전송 (Mock 응답) ──
    @Operation(summary = "Agent에 메시지 전송 (Mock)")
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@Valid @RequestBody ChatRequest request) {
        String reply = generateMockReply(request.provider(), request.dataset(), request.message());

        Map<String, Object> response = Map.of(
            "id", UUID.randomUUID().toString(),
            "provider", request.provider(),
            "dataset", request.dataset(),
            "message", reply,
            "timestamp", Instant.now().toString(),
            "mock", true
        );
        return ApiResponse.ok(response);
    }

    // ── Request DTO ──
    public record ChatRequest(
        @NotBlank String provider,
        @NotBlank String dataset,
        @NotBlank String message
    ) {}

    // ── Mock 응답 생성 ──
    private String generateMockReply(String provider, String dataset, String message) {
        String providerName = switch (provider) {
            case "chatgpt" -> "ChatGPT";
            case "gemini" -> "Gemini";
            case "claude" -> "Claude";
            default -> provider;
        };

        String datasetName = switch (dataset) {
            case "patients" -> "환자";
            case "trials" -> "임상시험";
            default -> dataset;
        };

        String lowerMsg = message.toLowerCase();

        // 데이터셋별 Mock 분석 응답
        if (dataset.equals("patients")) {
            if (lowerMsg.contains("통계") || lowerMsg.contains("요약") || lowerMsg.contains("summary")) {
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
            if (lowerMsg.contains("혈액형") || lowerMsg.contains("blood")) {
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

        if (dataset.equals("trials")) {
            if (lowerMsg.contains("통계") || lowerMsg.contains("요약") || lowerMsg.contains("summary")) {
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
            if (lowerMsg.contains("등록") || lowerMsg.contains("enroll")) {
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

        // 기본 응답
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
}

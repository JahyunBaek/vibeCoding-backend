package com.example.commonsystem.agent.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeminiChatProvider implements AgentChatProvider {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String MODEL = "gemini-3.0-flash";

    @Value("${spring.gemini.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public GeminiChatProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerId() {
        return "gemini";
    }

    @Override
    public boolean isLive() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String chat(String dataset, String message) {
        String systemPrompt = buildSystemPrompt(dataset);
        String url = String.format(GEMINI_API_URL, MODEL, apiKey);

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", message)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 2048
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);
            return extractText(response.getBody());
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API 호출에 실패했습니다: " + e.getMessage());
        }
    }

    private String extractText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "응답을 파싱할 수 없습니다.";
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            return "응답 처리 중 오류가 발생했습니다.";
        }
    }

    private String buildSystemPrompt(String dataset) {
        String dataContext = switch (dataset) {
            case "patients" -> """
                    당신은 의료 데이터 분석 전문 AI입니다.
                    현재 20명의 환자 데이터가 있으며, 다음 필드를 포함합니다:
                    - 환자ID, 이름, 성별(MALE/FEMALE), 생년월일, 혈액형(A+, B+, O+, AB+ 등)
                    - 진료과(IM:내과, GS:외과, PED:소아과, OB:산부인과, NS:신경외과, DR:피부과, OP:안과)
                    - 상태(ACTIVE:활성, DISCHARGED:퇴원, FOLLOW_UP:추적관찰, INACTIVE:비활성)
                    - 진단명, 담당의

                    데이터 통계:
                    - 성별: 남성 10명, 여성 10명
                    - 상태: ACTIVE 12명, DISCHARGED 3명, FOLLOW_UP 3명, INACTIVE 2명
                    - 혈액형: A+ 5명, B+ 4명, O+ 4명, AB+ 3명, A- 1명, B- 1명, O- 1명, AB- 1명
                    - 진료과: IM 5명, GS 3명, PED 3명, OB 2명, NS 3명, DR 2명, OP 2명
                    """;
            case "trials" -> """
                    당신은 임상시험 데이터 분석 전문 AI입니다.
                    현재 12건의 임상시험 데이터가 있으며, 다음 필드를 포함합니다:
                    - 시험ID, 시험명, 단계(PHASE_1~PHASE_4), 상태(ACTIVE/RECRUITING/COMPLETED/PLANNED/SUSPENDED)
                    - 목표 등록수, 현재 등록수, 시작일, 종료일, 책임연구자

                    데이터 통계:
                    - 상태: ACTIVE 5건, RECRUITING 2건, COMPLETED 2건, PLANNED 2건, SUSPENDED 1건
                    - 단계: Phase 1 3건, Phase 2 4건, Phase 3 3건, Phase 4 1건, Phase 1/2 1건
                    - 전체 등록률: 78.8% (2,287/2,739명)
                    - 등록 완료(100%): CT-2024-005(COPD 흡입제), CT-2024-012(아토피 피부염)
                    """;
            case "genomics" -> "당신은 임상유전체학(Clinical Genomics) 전문 AI입니다. ACMG 가이드라인, ClinVar, gnomAD, COSMIC 등의 지식을 활용합니다.";
            default -> "당신은 데이터 분석 전문 AI입니다.";
        };

        return dataContext + "\n사용자의 질문에 대해 데이터를 기반으로 정확하고 유용한 분석 결과를 제공하세요. " +
                "마크다운 형식을 사용하고, 이모지를 적절히 활용하세요. 한국어로 답변하세요.";
    }
}

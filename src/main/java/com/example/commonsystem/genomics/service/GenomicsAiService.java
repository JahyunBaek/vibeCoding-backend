package com.example.commonsystem.genomics.service;

import com.example.commonsystem.agent.service.AgentChatProvider;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantDetail;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;
import com.example.commonsystem.genomics.mapper.VariantMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Gemini 기반 유전체 AI 분석 서비스.
 * 변이 해석, 샘플 요약, 임상시험 매칭 추천을 제공한다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GenomicsAiService {

    private final List<AgentChatProvider> chatProviders;
    private final VariantMapper variantMapper;

    /**
     * 단일 변이의 임상적 의미를 AI로 해석한다.
     */
    public String interpretVariant(VariantDetail v) {
        String prompt = buildVariantPrompt(v);
        return callGemini(prompt);
    }

    /**
     * 샘플 내 전체 변이를 종합 분석한다.
     */
    public String summarizeSample(long sampleId, Long tenantId, List<VariantListRow> variants) {
        if (variants.isEmpty()) {
            return "해당 샘플에 검출된 변이가 없습니다.";
        }
        String prompt = buildSampleSummaryPrompt(variants);
        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        AgentChatProvider gemini = chatProviders.stream()
                .filter(p -> "gemini".equals(p.providerId()) && p.isLive())
                .findFirst()
                .orElse(null);

        if (gemini == null) {
            return "[Mock AI 응답] Gemini API 키가 설정되지 않았습니다. " +
                    "application-local.yml의 spring.gemini.api-key를 설정해주세요.\n\n" +
                    "실제 연동 시 다음 분석이 제공됩니다:\n" +
                    "- 변이의 임상적 의미 해석\n" +
                    "- ACMG 가이드라인 기반 분류 근거\n" +
                    "- 관련 문헌 및 데이터베이스 참조\n" +
                    "- 치료 옵션 제안";
        }

        return gemini.chat("genomics", prompt);
    }

    private String buildVariantPrompt(VariantDetail v) {
        return """
                당신은 임상유전체학 전문 AI입니다. 다음 변이의 임상적 의미를 분석해주세요.

                ## 변이 정보
                - **유전자**: %s
                - **위치**: %s:%d
                - **변이**: %s → %s (유형: %s)
                - **HGVS**: c.%s / p.%s
                - **영향**: %s (%s)
                - **접합성**: %s
                - **ACMG 분류**: %s
                - **gnomAD AF**: %s
                - **ClinVar**: %s
                - **COSMIC**: %s

                ## 분석 요청
                1. 이 변이의 **임상적 의미**를 설명해주세요.
                2. ACMG 가이드라인에 따른 **분류 근거**를 제시해주세요.
                3. 관련된 **질환**과 **유전 패턴**을 설명해주세요.
                4. 가능한 **치료 옵션**이나 **약물유전체 정보**가 있다면 언급해주세요.
                5. 참고할 수 있는 **문헌이나 데이터베이스**를 제시해주세요.

                한국어로 답변하되, 전문 용어는 영문 병기해주세요. 마크다운 형식을 사용하세요.
                """.formatted(
                v.geneSymbol(), v.chromosome(), v.position(),
                v.refAllele(), v.altAllele(), v.variantType(),
                nvl(v.hgvsC()), nvl(v.hgvsP()),
                nvl(v.consequence()), nvl(v.impact()),
                nvl(v.zygosity()), nvl(v.acmgClass()),
                v.gnomadAf() != null ? String.format("%.6f", v.gnomadAf()) : "N/A",
                nvl(v.clinvarId()), nvl(v.cosmicId()));
    }

    private String buildSampleSummaryPrompt(List<VariantListRow> variants) {
        long total = variants.size();
        long pathogenic = variants.stream().filter(v -> "PATHOGENIC".equals(v.acmgClass()) || "LIKELY_PATHOGENIC".equals(v.acmgClass())).count();
        long vus = variants.stream().filter(v -> "VUS".equals(v.acmgClass())).count();
        long highImpact = variants.stream().filter(v -> "HIGH".equals(v.impact())).count();

        String topVariants = variants.stream()
                .filter(v -> "PATHOGENIC".equals(v.acmgClass()) || "LIKELY_PATHOGENIC".equals(v.acmgClass()) || "HIGH".equals(v.impact()))
                .limit(20)
                .map(v -> "  - %s %s:%s %s→%s [%s, %s] %s".formatted(
                        v.geneSymbol(), v.chromosome(), String.valueOf(v.position()),
                        v.refAllele(), v.altAllele(),
                        nvl(v.acmgClass()), nvl(v.impact()), nvl(v.consequence())))
                .collect(Collectors.joining("\n"));

        return """
                당신은 임상유전체학 전문 AI입니다. 다음 샘플의 변이 데이터를 종합 분석해주세요.

                ## 샘플 요약 통계
                - **총 변이 수**: %d개
                - **Pathogenic/Likely Pathogenic**: %d개
                - **VUS**: %d개
                - **HIGH Impact**: %d개

                ## 주요 변이 목록 (Pathogenic + HIGH impact, 최대 20개)
                %s

                ## 분석 요청
                1. 전체적인 **변이 프로필 요약**을 작성해주세요.
                2. 임상적으로 중요한 **주요 소견(Key Findings)**을 정리해주세요.
                3. 추가로 확인이 필요한 **VUS 변이**에 대해 언급해주세요.
                4. **권장 후속 조치**(추가 검사, 유전 상담 등)를 제안해주세요.

                한국어로 답변하되, 전문 용어는 영문 병기해주세요. 마크다운 형식을 사용하세요.
                """.formatted(total, pathogenic, vus, highImpact,
                topVariants.isEmpty() ? "  (해당 변이 없음)" : topVariants);
    }

    private String nvl(String s) { return s != null ? s : "N/A"; }
}

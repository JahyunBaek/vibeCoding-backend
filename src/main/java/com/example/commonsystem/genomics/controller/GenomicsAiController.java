package com.example.commonsystem.genomics.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantDetail;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantFilter;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;
import com.example.commonsystem.genomics.mapper.VariantMapper;
import com.example.commonsystem.genomics.service.GenomicsAiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Genomics AI", description = "AI 기반 유전체 분석")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/ai")
public class GenomicsAiController {

    private final GenomicsAiService aiService;
    private final VariantMapper variantMapper;
    private final TenantContextHolder tenantCtx;

    @Operation(summary = "단일 변이 AI 해석")
    @PostMapping("/interpret/{variantId}")
    public ApiResponse<Map<String, String>> interpretVariant(@PathVariable long variantId) {
        VariantDetail v = variantMapper.findById(variantId, tenantCtx.currentTenantId());
        if (v == null) return ApiResponse.fail("NOT_FOUND", "변이를 찾을 수 없습니다.");
        String result = aiService.interpretVariant(v);
        return ApiResponse.ok(Map.of("interpretation", result));
    }

    @Operation(summary = "샘플 전체 변이 AI 요약")
    @PostMapping("/summarize/{sampleId}")
    public ApiResponse<Map<String, String>> summarizeSample(@PathVariable long sampleId) {
        Long tenantId = tenantCtx.currentTenantId();
        VariantFilter filter = new VariantFilter(sampleId, null, null, null, null, null, null, null);
        List<VariantListRow> variants = variantMapper.findPage(tenantId, filter, 10000, 0);
        String result = aiService.summarizeSample(sampleId, tenantId, variants);
        return ApiResponse.ok(Map.of("summary", result));
    }
}

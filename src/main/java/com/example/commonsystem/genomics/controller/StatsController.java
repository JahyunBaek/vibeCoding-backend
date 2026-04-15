package com.example.commonsystem.genomics.controller;

import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.StatsDtos.VariantStats;
import com.example.commonsystem.genomics.mapper.StatsMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Genomics Stats", description = "유전체 통계 대시보드")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/stats")
public class StatsController {

    private final StatsMapper statsMapper;
    private final TenantContextHolder tenantCtx;

    @Operation(summary = "변이 분포 통계 (전체 또는 샘플별)")
    @GetMapping
    public ApiResponse<VariantStats> stats(@RequestParam(required = false) Long sampleId) {
        Long tenantId = tenantCtx.currentTenantId();
        VariantStats stats = new VariantStats(
                statsMapper.totalVariants(tenantId, sampleId),
                statsMapper.totalSamples(tenantId),
                statsMapper.countByChromosome(tenantId, sampleId),
                statsMapper.countByVariantType(tenantId, sampleId),
                statsMapper.countByImpact(tenantId, sampleId),
                statsMapper.countByAcmgClass(tenantId, sampleId),
                statsMapper.topGenes(tenantId, sampleId, 15)
        );
        return ApiResponse.ok(stats);
    }
}

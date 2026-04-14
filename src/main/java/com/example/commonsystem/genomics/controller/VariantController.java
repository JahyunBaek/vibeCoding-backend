package com.example.commonsystem.genomics.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantDetail;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantFilter;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;
import com.example.commonsystem.genomics.service.VariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Variants", description = "변이 데이터 조회/필터링")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/variants")
public class VariantController {

    private final VariantService variantService;

    @Operation(summary = "변이 목록 조회 (필터링)")
    @GetMapping
    public ApiResponse<PageResponse<VariantListRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sampleId,
            @RequestParam(required = false) String geneSymbol,
            @RequestParam(required = false) String chromosome,
            @RequestParam(required = false) String variantType,
            @RequestParam(required = false) String impact,
            @RequestParam(required = false) String acmgClass,
            @RequestParam(required = false) Double gnomadAfMax,
            @RequestParam(required = false) String search) {
        VariantFilter filter = new VariantFilter(
                sampleId, geneSymbol, chromosome, variantType,
                impact, acmgClass, gnomadAfMax, search);
        return ApiResponse.ok(variantService.page(page, size, filter));
    }

    @Operation(summary = "변이 상세 조회")
    @GetMapping("/{variantId}")
    public ApiResponse<VariantDetail> detail(@PathVariable long variantId) {
        return ApiResponse.ok(variantService.detail(variantId));
    }
}

package com.example.commonsystem.genomics.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.PgxDtos.PgxListRow;
import com.example.commonsystem.genomics.dto.PgxDtos.PgxMatch;
import com.example.commonsystem.genomics.service.PgxService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "PGx", description = "약물유전체 (Pharmacogenomics)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/pgx")
public class PgxController {

    private final PgxService pgxService;

    @Operation(summary = "PGx 매핑 목록")
    @GetMapping
    public ApiResponse<PageResponse<PgxListRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ApiResponse.ok(pgxService.page(page, size, search));
    }

    @Operation(summary = "샘플 기반 PGx 매칭 조회")
    @GetMapping("/match/{sampleId}")
    public ApiResponse<List<PgxMatch>> matchBySample(@PathVariable long sampleId) {
        return ApiResponse.ok(pgxService.matchBySample(sampleId));
    }
}

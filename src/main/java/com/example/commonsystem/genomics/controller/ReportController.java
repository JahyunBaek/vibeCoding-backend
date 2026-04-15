package com.example.commonsystem.genomics.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.ReportDtos.ReportDetail;
import com.example.commonsystem.genomics.dto.ReportDtos.ReportListRow;
import com.example.commonsystem.genomics.service.ReportService;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Genomic Reports", description = "유전체 분석 보고서")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "보고서 목록")
    @GetMapping
    public ApiResponse<PageResponse<ReportListRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sampleId) {
        return ApiResponse.ok(reportService.page(page, size, sampleId));
    }

    @Operation(summary = "보고서 상세")
    @GetMapping("/{reportId}")
    public ApiResponse<ReportDetail> detail(@PathVariable long reportId) {
        return ApiResponse.ok(reportService.detail(reportId));
    }

    @Operation(summary = "보고서 자동 생성 (AI 요약 포함)")
    @PostMapping("/generate/{sampleId}")
    public ApiResponse<Void> generate(@PathVariable long sampleId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        reportService.generate(sampleId, principal.getUserId());
        return ApiResponse.ok();
    }

    @Operation(summary = "보고서 상태 변경")
    @PatchMapping("/{reportId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable long reportId,
                                          @RequestParam String status) {
        reportService.updateStatus(reportId, status);
        return ApiResponse.ok();
    }

    @Operation(summary = "보고서 삭제")
    @DeleteMapping("/{reportId}")
    public ApiResponse<Void> delete(@PathVariable long reportId) {
        reportService.delete(reportId);
        return ApiResponse.ok();
    }
}

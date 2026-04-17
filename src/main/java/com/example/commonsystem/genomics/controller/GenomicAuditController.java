package com.example.commonsystem.genomics.controller;

import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.AuditDtos.GenomicAuditRow;
import com.example.commonsystem.genomics.service.GenomicAuditService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Genomic Audit", description = "유전체 데이터 접근 감사 로그")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/audit")
public class GenomicAuditController {

    private final GenomicAuditService auditService;

    @Operation(summary = "감사 로그 조회")
    @GetMapping
    public ApiResponse<PageResponse<GenomicAuditRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType) {
        return ApiResponse.ok(auditService.page(page, size, action, resourceType));
    }
}

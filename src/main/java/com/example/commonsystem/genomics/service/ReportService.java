package com.example.commonsystem.genomics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.ReportDtos.ReportDetail;
import com.example.commonsystem.genomics.dto.ReportDtos.ReportListRow;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantFilter;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;
import com.example.commonsystem.genomics.mapper.ReportMapper;
import com.example.commonsystem.genomics.mapper.VariantMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportMapper reportMapper;
    private final VariantMapper variantMapper;
    private final GenomicsAiService aiService;
    private final TenantContextHolder tenantCtx;

    public PageResponse<ReportListRow> page(int page, int size, Long sampleId) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        long total = reportMapper.count(tenantId, sampleId);
        List<ReportListRow> items = reportMapper.findPage(tenantId, sampleId, s, offset);
        return new PageResponse<>(items, p, s, total);
    }

    public ReportDetail detail(long reportId) {
        return reportMapper.findById(reportId, tenantCtx.currentTenantId());
    }

    /**
     * AI 요약 포함 보고서 자동 생성
     */
    @Transactional
    public long generate(long sampleId, Long createdBy) {
        Long tenantId = tenantCtx.currentTenantId();

        // 변이 조회
        VariantFilter filter = new VariantFilter(sampleId, null, null, null, null, null, null, null);
        List<VariantListRow> variants = variantMapper.findPage(tenantId, filter, 10000, 0);
        int total = variants.size();
        int pathogenic = (int) variants.stream()
                .filter(v -> "PATHOGENIC".equals(v.acmgClass()) || "LIKELY_PATHOGENIC".equals(v.acmgClass()))
                .count();

        // AI 요약
        String summary = aiService.summarizeSample(sampleId, tenantId, variants);

        String title = "Genomic Analysis Report - %d variants".formatted(total);
        reportMapper.insert(tenantId, sampleId, title, summary, total, pathogenic, createdBy);
        return sampleId;
    }

    @Transactional
    public void updateStatus(long reportId, String status) {
        reportMapper.updateStatus(reportId, status, tenantCtx.currentTenantId());
    }

    @Transactional
    public void delete(long reportId) {
        reportMapper.delete(reportId, tenantCtx.currentTenantId());
    }
}

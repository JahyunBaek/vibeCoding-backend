package com.example.commonsystem.genomics.dto;

import java.time.LocalDateTime;

public class VariantDtos {

    // ── 목록 조회 ──
    public record VariantListRow(
        long variantId,
        long sampleId,
        String sampleNo,
        String geneSymbol,
        String chromosome,
        long position,
        String refAllele,
        String altAllele,
        String variantType,
        String zygosity,
        String hgvsC,
        String hgvsP,
        String consequence,
        String impact,
        String acmgClass,
        Double gnomadAf,
        String clinvarId,
        String cosmicId,
        LocalDateTime createdAt
    ) {}

    // ── 단건 조회 ──
    public record VariantDetail(
        long variantId,
        long sampleId,
        String sampleNo,
        long tenantId,
        String geneSymbol,
        String chromosome,
        long position,
        String refAllele,
        String altAllele,
        String variantType,
        String zygosity,
        Double quality,
        Integer readDepth,
        Double alleleFreq,
        String hgvsC,
        String hgvsP,
        String consequence,
        String impact,
        String acmgClass,
        String clinvarId,
        Double gnomadAf,
        String cosmicId,
        LocalDateTime createdAt,
        // 조인 데이터
        String patientName,
        String panelName
    ) {}

    // ── 필터 파라미터 ──
    public record VariantFilter(
        Long sampleId,
        String geneSymbol,
        String chromosome,
        String variantType,
        String impact,
        String acmgClass,
        Double gnomadAfMax,
        String search
    ) {}
}

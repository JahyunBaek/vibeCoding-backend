package com.example.commonsystem.genomics.domain;

import java.time.LocalDateTime;

public record Variant(
    long variantId,
    long sampleId,
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
    LocalDateTime createdAt
) {}

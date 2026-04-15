package com.example.commonsystem.genomics.dto;

import java.util.List;

public class StatsDtos {

    public record CountItem(String label, long count) {}

    public record VariantStats(
        long totalVariants,
        long totalSamples,
        List<CountItem> byChromosome,
        List<CountItem> byVariantType,
        List<CountItem> byImpact,
        List<CountItem> byAcmgClass,
        List<CountItem> topGenes
    ) {}
}

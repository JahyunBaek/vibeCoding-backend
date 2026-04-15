package com.example.commonsystem.genomics.dto;

public class PgxDtos {

    public record PgxListRow(
        long pgxId,
        String geneSymbol,
        String variantName,
        String drugName,
        String effect,
        String recommendation,
        String evidenceLevel,
        String source
    ) {}

    /** 환자 변이와 매칭된 PGx 결과 */
    public record PgxMatch(
        String geneSymbol,
        String variantName,
        String drugName,
        String effect,
        String recommendation,
        String evidenceLevel,
        String source,
        long variantId,
        String acmgClass
    ) {}
}

package com.example.commonsystem.genomics.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PanelDtos {

    // ── 목록 조회 ──
    public record PanelListRow(
        long panelId,
        String panelCode,
        String name,
        String category,
        int geneCount,
        boolean useYn,
        LocalDateTime createdAt
    ) {}

    // ── 단건 조회 (유전자 목록 포함) ──
    public record PanelDetail(
        long panelId,
        String panelCode,
        String name,
        String description,
        String category,
        int geneCount,
        boolean useYn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PanelGeneItem> genes
    ) {}

    public record PanelGeneItem(
        long panelGeneId,
        String geneSymbol,
        String chromosome,
        String description
    ) {}

    // ── 생성 ──
    @Getter @Setter
    @NoArgsConstructor
    public static class PanelCreateCommand {
        private Long panelId;
        private Long tenantId;
        @NotBlank private String panelCode;
        @NotBlank private String name;
        private String description;
        @NotBlank private String category;
        private List<PanelGeneInput> genes;
    }

    // ── 수정 ──
    @Getter @Setter
    @NoArgsConstructor
    public static class PanelUpdateCommand {
        private long panelId;
        private String name;
        private String description;
        private String category;
        private boolean useYn;
        private List<PanelGeneInput> genes;
    }

    // ── 유전자 입력 ──
    public record PanelGeneInput(
        @NotBlank String geneSymbol,
        String chromosome,
        String description
    ) {}
}

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

    // ── 단건 조회 (유전자 목록 포함, MyBatis collection 매핑에 setter 필요) ──
    @Getter @Setter
    @NoArgsConstructor
    public static class PanelDetail {
        private long panelId;
        private String panelCode;
        private String name;
        private String description;
        private String category;
        private int geneCount;
        private boolean useYn;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<PanelGeneItem> genes;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class PanelGeneItem {
        private long panelGeneId;
        private String geneSymbol;
        private String chromosome;
        private String description;
    }

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

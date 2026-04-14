package com.example.commonsystem.genomics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SampleDtos {

    // ── 목록 조회 ──
    public record SampleListRow(
        long sampleId,
        String sampleNo,
        long patientId,
        String patientName,
        String sampleType,
        String panelName,
        String status,
        LocalDate receivedDate,
        LocalDate completedDate,
        LocalDateTime createdAt
    ) {}

    // ── 단건 조회 ──
    public record SampleDetail(
        long sampleId,
        long tenantId,
        long patientId,
        String patientName,
        String sampleNo,
        String sampleType,
        Long panelId,
        String panelName,
        String status,
        LocalDate receivedDate,
        LocalDate completedDate,
        String note,
        Long createdBy,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int variantCount
    ) {}

    // ── 생성 ──
    @Getter @Setter
    @NoArgsConstructor
    public static class SampleCreateCommand {
        private Long sampleId;
        private Long tenantId;
        @NotNull  private Long patientId;
        @NotBlank private String sampleType;
        private Long panelId;
        private String note;
        private Long createdBy;

        public SampleCreateCommand(Long tenantId, Long patientId, String sampleType,
                                   Long panelId, String note, Long createdBy) {
            this.tenantId = tenantId;
            this.patientId = patientId;
            this.sampleType = sampleType;
            this.panelId = panelId;
            this.note = note;
            this.createdBy = createdBy;
        }
    }

    // ── 상태 변경 ──
    public record StatusUpdateRequest(
        @NotBlank String status
    ) {}

    // ── 수정 ──
    @Getter @Setter
    @NoArgsConstructor
    public static class SampleUpdateCommand {
        private long sampleId;
        private String sampleType;
        private Long panelId;
        private String note;
    }
}

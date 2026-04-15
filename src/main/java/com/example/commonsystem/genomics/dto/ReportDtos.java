package com.example.commonsystem.genomics.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ReportDtos {

    public record ReportListRow(
        long reportId,
        long sampleId,
        String sampleNo,
        String title,
        int variantCount,
        int pathogenicCount,
        String status,
        String createdByName,
        LocalDateTime createdAt
    ) {}

    @Getter @Setter
    @NoArgsConstructor
    public static class ReportDetail {
        private long reportId;
        private long tenantId;
        private long sampleId;
        private String sampleNo;
        private String title;
        private String summary;
        private int variantCount;
        private int pathogenicCount;
        private String status;
        private Long createdBy;
        private String createdByName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

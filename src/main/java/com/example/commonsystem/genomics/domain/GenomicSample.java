package com.example.commonsystem.genomics.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GenomicSample(
    long sampleId,
    long tenantId,
    long patientId,
    String sampleNo,
    String sampleType,
    Long panelId,
    String status,
    LocalDate receivedDate,
    LocalDate completedDate,
    String note,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

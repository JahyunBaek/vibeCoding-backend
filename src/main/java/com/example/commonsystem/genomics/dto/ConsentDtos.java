package com.example.commonsystem.genomics.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsentDtos {

    public record ConsentListRow(
        long consentId,
        long patientId,
        Long sampleId,
        String sampleNo,
        String consentType,
        String status,
        LocalDateTime signedAt,
        String signedByName,
        LocalDate expiresAt,
        LocalDateTime createdAt
    ) {}

    public record ConsentDetail(
        long consentId,
        long tenantId,
        long patientId,
        Long sampleId,
        String sampleNo,
        String consentType,
        String status,
        LocalDateTime signedAt,
        LocalDateTime revokedAt,
        LocalDate expiresAt,
        String signedByName,
        String witnessName,
        String note,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record CreateRequest(
        @NotNull Long patientId,
        Long sampleId,
        @NotBlank String consentType,
        LocalDate expiresAt,
        String note
    ) {}

    public record SignRequest(
        @NotBlank String signedByName,
        String witnessName
    ) {}
}

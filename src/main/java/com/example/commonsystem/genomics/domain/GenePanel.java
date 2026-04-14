package com.example.commonsystem.genomics.domain;

import java.time.LocalDateTime;

public record GenePanel(
    long panelId,
    long tenantId,
    String panelCode,
    String name,
    String description,
    String category,
    int geneCount,
    boolean useYn,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

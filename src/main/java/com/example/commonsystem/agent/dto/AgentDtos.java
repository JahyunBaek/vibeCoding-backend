package com.example.commonsystem.agent.dto;

import jakarta.validation.constraints.NotBlank;

public class AgentDtos {

    // ── Request ──

    public record ChatRequest(
        @NotBlank String provider,
        @NotBlank String dataset,
        @NotBlank String message
    ) {}

    // ── Response ──

    public record ProviderInfo(
        String id,
        String name,
        String model,
        String description,
        String icon,
        boolean available,
        boolean mock
    ) {}

    public record DatasetInfo(
        String id,
        String name,
        String description,
        int recordCount
    ) {}

    public record ChatResponse(
        String id,
        String provider,
        String dataset,
        String message,
        String timestamp,
        boolean mock
    ) {}
}

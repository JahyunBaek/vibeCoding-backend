package com.example.commonsystem.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeCreateCommand(
    String groupKey,
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 200) String value,
    int sortOrder,
    boolean useYn,
    Long tenantId
) {}

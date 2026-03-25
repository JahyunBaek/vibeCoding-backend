package com.example.commonsystem.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeUpdateCommand(
    String groupKey,
    String code,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 200) String value,
    int sortOrder,
    boolean useYn,
    Long tenantId
) {}

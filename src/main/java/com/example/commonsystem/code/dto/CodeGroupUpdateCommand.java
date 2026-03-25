package com.example.commonsystem.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeGroupUpdateCommand(
    String groupKey,
    @NotBlank @Size(max = 100) String groupName,
    boolean useYn,
    Long tenantId
) {}

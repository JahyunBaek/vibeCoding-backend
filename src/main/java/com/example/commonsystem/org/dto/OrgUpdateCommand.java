package com.example.commonsystem.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrgUpdateCommand(
    long orgId,
    Long parentId,
    @NotBlank @Size(max = 100) String name,
    int sortOrder,
    boolean useYn
) {}

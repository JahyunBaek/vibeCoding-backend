package com.example.commonsystem.org.dto;

public record OrgCreateCommand(
    Long parentId,
    String name,
    int sortOrder,
    boolean useYn,
    Long tenantId
) {}

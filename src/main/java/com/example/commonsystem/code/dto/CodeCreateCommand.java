package com.example.commonsystem.code.dto;

public record CodeCreateCommand(
    String groupKey,
    String code,
    String name,
    String value,
    int sortOrder,
    boolean useYn,
    Long tenantId
) {}

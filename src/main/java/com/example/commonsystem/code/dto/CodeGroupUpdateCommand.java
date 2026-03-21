package com.example.commonsystem.code.dto;

public record CodeGroupUpdateCommand(
    String groupKey,
    String groupName,
    boolean useYn,
    Long tenantId
) {}

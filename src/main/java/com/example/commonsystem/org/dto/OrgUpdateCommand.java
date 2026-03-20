package com.example.commonsystem.org.dto;

public record OrgUpdateCommand(
    long orgId,
    Long parentId,
    String name,
    int sortOrder,
    boolean useYn
) {}

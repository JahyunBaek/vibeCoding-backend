package com.example.commonsystem.org;

public record Org(
    long orgId,
    Long parentId,
    String name,
    int sortOrder,
    boolean useYn
) {}

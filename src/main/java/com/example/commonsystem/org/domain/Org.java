package com.example.commonsystem.org.domain;

public record Org(
    long orgId,
    Long parentId,
    String name,
    int sortOrder,
    boolean useYn
) {}

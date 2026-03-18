package com.example.commonsystem.org;

public record OrgCreateCommand(
    Long parentId,
    String name,
    int sortOrder,
    boolean useYn
) {}

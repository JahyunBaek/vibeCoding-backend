package com.example.commonsystem.code.domain;

public record CodeItem(
    String groupKey,
    String code,
    String name,
    String value,
    int sortOrder,
    boolean useYn
) {}

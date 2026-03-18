package com.example.commonsystem.code;

public record CodeItem(
    String groupKey,
    String code,
    String name,
    String value,
    int sortOrder,
    boolean useYn
) {}

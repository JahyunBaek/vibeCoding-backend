package com.example.commonsystem.code.dto;

public record CodeUpdateCommand(
    String groupKey,
    String code,
    String name,
    String value,
    int sortOrder,
    boolean useYn
) {}

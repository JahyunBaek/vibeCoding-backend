package com.example.commonsystem.code.dto;

public record CodeGroupCreateCommand(
    String groupKey,
    String groupName,
    boolean useYn
) {}

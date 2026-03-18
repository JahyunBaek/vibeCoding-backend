package com.example.commonsystem.code;

public record CodeGroupCreateCommand(
    String groupKey,
    String groupName,
    boolean useYn
) {}

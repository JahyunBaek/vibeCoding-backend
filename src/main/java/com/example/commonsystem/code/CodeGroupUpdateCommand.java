package com.example.commonsystem.code;

public record CodeGroupUpdateCommand(
    String groupKey,
    String groupName,
    boolean useYn
) {}

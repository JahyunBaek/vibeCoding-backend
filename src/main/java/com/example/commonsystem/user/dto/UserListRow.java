package com.example.commonsystem.user.dto;

public record UserListRow(
    long userId,
    String username,
    String name,
    String roleKey,
    Long orgId,
    boolean enabled
) {}

package com.example.commonsystem.user.domain;

public record User(
    long userId,
    String username,
    String passwordHash,
    String name,
    String roleKey,
    Long orgId,
    Long tenantId,   // null = SUPER_ADMIN
    boolean enabled
) {}

package com.example.commonsystem.user;

public record User(
    long userId,
    String username,
    String passwordHash,
    String name,
    String roleKey,
    Long orgId,
    boolean enabled
) {}

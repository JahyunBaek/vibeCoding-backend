package com.example.commonsystem.user;

public record UserUpdateCommand(
    long userId,
    String passwordHash,
    String name,
    String roleKey,
    Long orgId,
    boolean enabled
) {}

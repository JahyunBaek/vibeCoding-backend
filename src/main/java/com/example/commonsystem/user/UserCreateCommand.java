package com.example.commonsystem.user;

public record UserCreateCommand(
    String username,
    String passwordHash,
    String name,
    String roleKey,
    Long orgId,
    boolean enabled
) {}

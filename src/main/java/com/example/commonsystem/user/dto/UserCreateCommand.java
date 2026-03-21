package com.example.commonsystem.user.dto;

public record UserCreateCommand(
    String username,
    String passwordHash,
    String name,
    String roleKey,
    Long orgId,
    Long tenantId,
    boolean enabled
) {}

package com.example.commonsystem.role;

public record RoleUpdateCommand(
    String roleKey,
    String roleName,
    boolean useYn
) {}

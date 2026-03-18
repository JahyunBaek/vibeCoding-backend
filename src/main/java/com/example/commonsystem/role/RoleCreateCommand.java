package com.example.commonsystem.role;

public record RoleCreateCommand(
    String roleKey,
    String roleName,
    boolean useYn
) {}

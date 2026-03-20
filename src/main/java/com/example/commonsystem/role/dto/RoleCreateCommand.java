package com.example.commonsystem.role.dto;

public record RoleCreateCommand(
    String roleKey,
    String roleName,
    boolean useYn
) {}

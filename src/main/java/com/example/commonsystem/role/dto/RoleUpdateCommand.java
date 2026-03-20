package com.example.commonsystem.role.dto;

public record RoleUpdateCommand(
    String roleKey,
    String roleName,
    boolean useYn
) {}

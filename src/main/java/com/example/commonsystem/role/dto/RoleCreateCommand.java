package com.example.commonsystem.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreateCommand(
    @NotBlank @Size(max = 50) String roleKey,
    @NotBlank @Size(max = 100) String roleName,
    boolean useYn
) {}

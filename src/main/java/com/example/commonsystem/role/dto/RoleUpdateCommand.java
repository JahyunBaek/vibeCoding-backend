package com.example.commonsystem.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpdateCommand(
    String roleKey,
    @NotBlank @Size(max = 100) String roleName,
    boolean useYn
) {}

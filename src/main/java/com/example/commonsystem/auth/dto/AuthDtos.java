package com.example.commonsystem.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password
  ) {}

  public record TokenResponse(
      String accessToken,
      UserSummary user
  ) {}

  public record ResetPasswordRequest(
      @NotBlank String token,
      @NotBlank @Size(min = 8, max = 100) String newPassword
  ) {}

  public record ResetTokenResponse(
      String token,
      int expiresInMinutes
  ) {}

  public record UserSummary(
      long userId,
      String username,
      String name,
      String roleKey,
      Long orgId,
      Long tenantId   // null = SUPER_ADMIN
  ) {}
}

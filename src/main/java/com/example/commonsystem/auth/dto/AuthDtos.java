package com.example.commonsystem.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password
  ) {}

  public record TokenResponse(
      String accessToken,
      UserSummary user
  ) {}

  public record UserSummary(
      long userId,
      String username,
      String name,
      String roleKey,
      Long orgId
  ) {}
}

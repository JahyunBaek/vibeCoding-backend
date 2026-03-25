package com.example.commonsystem.user.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.user.domain.User;
import com.example.commonsystem.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "내 정보", description = "내 정보 조회 및 수정")
@RestController
@RequestMapping("/api/me")
public class MeController {

  private final UserService userService;

  public MeController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "내 정보 조회")
  @GetMapping
  public ApiResponse<User> me(@AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(userService.me(principal.getUserId()));
  }

  public record UpdateMeRequest(
      @NotBlank String name,
      String currentPassword,
      String newPassword
  ) {}

  @Operation(summary = "내 정보 수정")
  @PutMapping
  public ApiResponse<Void> update(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdateMeRequest req) {
    userService.updateMe(principal.getUserId(), req.name(), req.currentPassword(), req.newPassword());
    return ApiResponse.ok();
  }
}

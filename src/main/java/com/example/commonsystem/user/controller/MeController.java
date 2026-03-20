package com.example.commonsystem.user.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.user.domain.User;
import com.example.commonsystem.user.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MeController {

  private final UserService userService;

  public MeController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<User> me(@AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(userService.me(principal.getUserId()));
  }

  public record UpdateMeRequest(
      @NotBlank String name,
      String currentPassword,
      String newPassword
  ) {}

  @PutMapping
  public ApiResponse<Void> update(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdateMeRequest req) {
    userService.updateMe(principal.getUserId(), req.name(), req.currentPassword(), req.newPassword());
    return ApiResponse.ok();
  }
}

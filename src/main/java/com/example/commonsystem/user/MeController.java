package com.example.commonsystem.user;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.security.UserPrincipal;
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
      String newPassword
  ) {}

  @PutMapping
  public ApiResponse<Void> update(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdateMeRequest req) {
    userService.updateMe(principal.getUserId(), req.name(), req.newPassword());
    return ApiResponse.ok();
  }
}

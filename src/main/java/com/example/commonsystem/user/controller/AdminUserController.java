package com.example.commonsystem.user.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.user.dto.UserListRow;
import com.example.commonsystem.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminUserController {

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<PageResponse<UserListRow>> list(
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Long tenantId
  ) {
    return ApiResponse.ok(userService.page(orgId, page, size, tenantId));
  }

  public record CreateUserRequest(
      String username,
      String password,
      String name,
      String roleKey,
      Long orgId,
      Boolean enabled,
      Long tenantId
  ) {}

  @PostMapping
  public ApiResponse<Void> create(@RequestBody CreateUserRequest req) {
    userService.create(req.username(), req.password(), req.name(), req.roleKey(), req.orgId(), req.enabled() == null || req.enabled(), req.tenantId());
    return ApiResponse.ok();
  }

  public record UpdateUserRequest(
      String password,
      String name,
      String roleKey,
      Long orgId,
      Boolean enabled
  ) {}

  @PutMapping("/{userId}")
  public ApiResponse<Void> update(@PathVariable long userId, @RequestBody UpdateUserRequest req) {
    userService.update(userId, req.name(), req.password(), req.roleKey(), req.orgId(), req.enabled() == null || req.enabled());
    return ApiResponse.ok();
  }

  public record ResetPasswordRequest(String newPassword) {}

  @PatchMapping("/{userId}/password")
  public ApiResponse<Void> resetPassword(@PathVariable long userId, @RequestBody ResetPasswordRequest req) {
    userService.adminResetPassword(userId, req.newPassword());
    return ApiResponse.ok();
  }

  @DeleteMapping("/{userId}")
  public ApiResponse<Void> delete(@PathVariable long userId) {
    userService.delete(userId);
    return ApiResponse.ok();
  }
}

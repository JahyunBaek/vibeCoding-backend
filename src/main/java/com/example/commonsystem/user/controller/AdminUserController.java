package com.example.commonsystem.user.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.user.dto.UserListRow;
import com.example.commonsystem.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<PageResponse<UserListRow>> list(
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(userService.page(orgId, page, size));
  }

  public record CreateUserRequest(
      String username,
      String password,
      String name,
      String roleKey,
      Long orgId,
      Boolean enabled
  ) {}

  @PostMapping
  public ApiResponse<Void> create(@RequestBody CreateUserRequest req) {
    userService.create(req.username(), req.password(), req.name(), req.roleKey(), req.orgId(), req.enabled() == null || req.enabled());
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

  @DeleteMapping("/{userId}")
  public ApiResponse<Void> delete(@PathVariable long userId) {
    userService.delete(userId);
    return ApiResponse.ok();
  }
}

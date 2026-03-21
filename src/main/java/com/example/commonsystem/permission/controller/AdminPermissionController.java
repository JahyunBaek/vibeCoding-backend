package com.example.commonsystem.permission.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.domain.Screen;
import com.example.commonsystem.permission.domain.ScreenAction;
import com.example.commonsystem.permission.service.PermissionService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminPermissionController {

  private final PermissionService permissionService;

  public AdminPermissionController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  // --- Screens ---
  @GetMapping("/screens")
  public ApiResponse<List<Screen>> screens() {
    return ApiResponse.ok(permissionService.allScreens());
  }

  public record CreateScreenReq(@NotBlank String screenKey, @NotBlank String screenName) {}

  @PostMapping("/screens")
  public ApiResponse<Void> createScreen(@RequestBody CreateScreenReq req) {
    permissionService.createScreen(req.screenKey(), req.screenName());
    return ApiResponse.ok();
  }

  public record UpdateScreenReq(@NotBlank String screenName, boolean useYn) {}

  @PutMapping("/screens/{screenId}")
  public ApiResponse<Void> updateScreen(@PathVariable int screenId, @RequestBody UpdateScreenReq req) {
    permissionService.updateScreen(screenId, req.screenName(), req.useYn());
    return ApiResponse.ok();
  }

  @DeleteMapping("/screens/{screenId}")
  public ApiResponse<Void> deleteScreen(@PathVariable int screenId) {
    permissionService.deleteScreen(screenId);
    return ApiResponse.ok();
  }

  // --- Actions ---
  @GetMapping("/screens/{screenId}/actions")
  public ApiResponse<List<ScreenAction>> actions(@PathVariable int screenId) {
    return ApiResponse.ok(permissionService.actionsByScreen(screenId));
  }

  public record CreateActionReq(@NotBlank String actionKey, @NotBlank String actionName) {}

  @PostMapping("/screens/{screenId}/actions")
  public ApiResponse<Void> createAction(@PathVariable int screenId, @RequestBody CreateActionReq req) {
    permissionService.createAction(screenId, req.actionKey(), req.actionName());
    return ApiResponse.ok();
  }

  public record UpdateActionReq(@NotBlank String actionName, boolean useYn) {}

  @PutMapping("/actions/{actionId}")
  public ApiResponse<Void> updateAction(@PathVariable int actionId, @RequestBody UpdateActionReq req) {
    permissionService.updateAction(actionId, req.actionName(), req.useYn());
    return ApiResponse.ok();
  }

  @DeleteMapping("/actions/{actionId}")
  public ApiResponse<Void> deleteAction(@PathVariable int actionId) {
    permissionService.deleteAction(actionId);
    return ApiResponse.ok();
  }

  // --- Role-Action Mapping ---
  @GetMapping("/actions/{actionId}/roles")
  public ApiResponse<List<String>> rolesByAction(@PathVariable int actionId) {
    return ApiResponse.ok(permissionService.rolesByAction(actionId));
  }

  public record SetRolesReq(List<String> roleKeys) {}

  @PutMapping("/actions/{actionId}/roles")
  public ApiResponse<Void> setRoles(@PathVariable int actionId, @RequestBody SetRolesReq req) {
    permissionService.setRoleActions(actionId, req.roleKeys());
    return ApiResponse.ok();
  }
}

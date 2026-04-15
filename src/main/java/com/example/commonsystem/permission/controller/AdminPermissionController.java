package com.example.commonsystem.permission.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.domain.Screen;
import com.example.commonsystem.permission.domain.ScreenAction;
import com.example.commonsystem.permission.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 권한", description = "화면/액션 권한 관리")
@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminPermissionController {

  private final PermissionService permissionService;

  public AdminPermissionController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  // --- Screens ---
  @Operation(summary = "화면 목록 조회")
  @GetMapping("/screens")
  public ApiResponse<List<Screen>> screens() {
    return ApiResponse.ok(permissionService.allScreens());
  }

  public record CreateScreenReq(@NotBlank String screenKey, @NotBlank String screenName) {}

  @Operation(summary = "화면 생성")
  @PostMapping("/screens")
  public ApiResponse<Void> createScreen(@Valid @RequestBody CreateScreenReq req) {
    permissionService.createScreen(req.screenKey(), req.screenName());
    return ApiResponse.ok();
  }

  public record UpdateScreenReq(@NotBlank String screenName, boolean useYn) {}

  @Operation(summary = "화면 수정")
  @PutMapping("/screens/{screenId}")
  public ApiResponse<Void> updateScreen(@PathVariable int screenId, @Valid @RequestBody UpdateScreenReq req) {
    permissionService.updateScreen(screenId, req.screenName(), req.useYn());
    return ApiResponse.ok();
  }

  @Operation(summary = "화면 삭제")
  @DeleteMapping("/screens/{screenId}")
  public ApiResponse<Void> deleteScreen(@PathVariable int screenId) {
    permissionService.deleteScreen(screenId);
    return ApiResponse.ok();
  }

  // --- Actions ---
  @Operation(summary = "화면별 액션 목록 조회")
  @GetMapping("/screens/{screenId}/actions")
  public ApiResponse<List<ScreenAction>> actions(@PathVariable int screenId) {
    return ApiResponse.ok(permissionService.actionsByScreen(screenId));
  }

  public record CreateActionReq(@NotBlank String actionKey, @NotBlank String actionName) {}

  @Operation(summary = "액션 생성")
  @PostMapping("/screens/{screenId}/actions")
  public ApiResponse<Void> createAction(@PathVariable int screenId, @Valid @RequestBody CreateActionReq req) {
    permissionService.createAction(screenId, req.actionKey(), req.actionName());
    return ApiResponse.ok();
  }

  public record UpdateActionReq(@NotBlank String actionName, boolean useYn) {}

  @Operation(summary = "액션 수정")
  @PutMapping("/actions/{actionId}")
  public ApiResponse<Void> updateAction(@PathVariable int actionId, @Valid @RequestBody UpdateActionReq req) {
    permissionService.updateAction(actionId, req.actionName(), req.useYn());
    return ApiResponse.ok();
  }

  @Operation(summary = "액션 삭제")
  @DeleteMapping("/actions/{actionId}")
  public ApiResponse<Void> deleteAction(@PathVariable int actionId) {
    permissionService.deleteAction(actionId);
    return ApiResponse.ok();
  }

  // --- Role-Action Mapping ---
  @Operation(summary = "액션별 역할 목록 조회")
  @GetMapping("/actions/{actionId}/roles")
  public ApiResponse<List<String>> rolesByAction(@PathVariable int actionId) {
    return ApiResponse.ok(permissionService.rolesByAction(actionId));
  }

  public record SetRolesReq(List<String> roleKeys) {}

  @Operation(summary = "액션 역할 매핑 설정")
  @PutMapping("/actions/{actionId}/roles")
  public ApiResponse<Void> setRoles(@PathVariable int actionId, @RequestBody SetRolesReq req) {
    permissionService.setRoleActions(actionId, req.roleKeys());
    return ApiResponse.ok();
  }
}

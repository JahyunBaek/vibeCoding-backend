package com.example.commonsystem.permission.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.dto.UserPermission;
import com.example.commonsystem.permission.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "권한", description = "사용자 권한 조회")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

  private final PermissionService permissionService;

  public PermissionController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @Operation(summary = "내 권한 목록 조회")
  @GetMapping("/my")
  public ApiResponse<List<UserPermission>> my() {
    return ApiResponse.ok(permissionService.myPermissions());
  }
}

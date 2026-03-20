package com.example.commonsystem.permission.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.dto.UserPermission;
import com.example.commonsystem.permission.service.PermissionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

  private final PermissionService permissionService;

  public PermissionController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @GetMapping("/my")
  public ApiResponse<List<UserPermission>> my() {
    return ApiResponse.ok(permissionService.myPermissions());
  }
}

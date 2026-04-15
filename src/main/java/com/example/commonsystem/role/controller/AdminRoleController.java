package com.example.commonsystem.role.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.role.domain.Role;
import com.example.commonsystem.role.dto.RoleCreateCommand;
import com.example.commonsystem.role.dto.RoleUpdateCommand;
import com.example.commonsystem.role.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 역할", description = "역할 관리")
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminRoleController {

  private final RoleService roleService;

  public AdminRoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @Operation(summary = "역할 목록 조회")
  @GetMapping
  public ApiResponse<PageResponse<Role>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(roleService.page(page, size));
  }

  @Operation(summary = "전체 역할 조회")
  @GetMapping("/all")
  public ApiResponse<List<Role>> all() {
    return ApiResponse.ok(roleService.all());
  }

  @Operation(summary = "역할 생성")
  @PostMapping
  public ApiResponse<Void> create(@Valid @RequestBody RoleCreateCommand cmd) {
    roleService.create(cmd);
    return ApiResponse.ok();
  }

  @Operation(summary = "역할 수정")
  @PutMapping("/{roleKey}")
  public ApiResponse<Void> update(@PathVariable String roleKey, @Valid @RequestBody RoleUpdateCommand cmd) {
    roleService.update(new RoleUpdateCommand(roleKey, cmd.roleName(), cmd.useYn()));
    return ApiResponse.ok();
  }

  @Operation(summary = "역할 삭제")
  @DeleteMapping("/{roleKey}")
  public ApiResponse<Void> delete(@PathVariable String roleKey) {
    roleService.delete(roleKey);
    return ApiResponse.ok();
  }
}

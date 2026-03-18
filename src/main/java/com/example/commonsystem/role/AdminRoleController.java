package com.example.commonsystem.role;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

  private final RoleService roleService;

  public AdminRoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping
  public ApiResponse<PageResponse<Role>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(roleService.page(page, size));
  }

  @GetMapping("/all")
  public ApiResponse<List<Role>> all() {
    return ApiResponse.ok(roleService.all());
  }

  @PostMapping
  public ApiResponse<Void> create(@RequestBody RoleCreateCommand cmd) {
    roleService.create(cmd);
    return ApiResponse.ok();
  }

  @PutMapping("/{roleKey}")
  public ApiResponse<Void> update(@PathVariable String roleKey, @RequestBody RoleUpdateCommand cmd) {
    roleService.update(new RoleUpdateCommand(roleKey, cmd.roleName(), cmd.useYn()));
    return ApiResponse.ok();
  }

  @DeleteMapping("/{roleKey}")
  public ApiResponse<Void> delete(@PathVariable String roleKey) {
    roleService.delete(roleKey);
    return ApiResponse.ok();
  }
}

package com.example.commonsystem.menu.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.menu.dto.MenuCreateCommand;
import com.example.commonsystem.menu.dto.MenuNode;
import com.example.commonsystem.menu.dto.MenuUpdateCommand;
import com.example.commonsystem.menu.service.MenuService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/menus")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminMenuController {

  private final MenuService menuService;

  public AdminMenuController(MenuService menuService) {
    this.menuService = menuService;
  }

  @GetMapping("/tree")
  public ApiResponse<List<MenuNode>> tree(@RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(menuService.getAllMenuTree(tenantId));
  }

  public record CreateMenuRequest(
      Long parentId,
      String name,
      String path,
      String icon,
      Integer sortOrder,
      Boolean useYn,
      String menuType,
      Long boardId,
      List<String> roleKeys,
      Long tenantId
  ) {}

  @PostMapping
  public ApiResponse<Long> create(@RequestBody CreateMenuRequest req) {
    MenuCreateCommand cmd = new MenuCreateCommand(
        req.parentId(),
        req.name(),
        req.path(),
        req.icon(),
        req.sortOrder() == null ? 0 : req.sortOrder(),
        req.useYn() == null || req.useYn(),
        req.menuType() == null ? "MENU" : req.menuType(),
        req.boardId(),
        req.tenantId()
    );
    long id = menuService.create(cmd, req.roleKeys());
    return ApiResponse.ok(id);
  }

  public record UpdateMenuRequest(
      Long parentId,
      String name,
      String path,
      String icon,
      Integer sortOrder,
      Boolean useYn
  ) {}

  @PutMapping("/{menuId}")
  public ApiResponse<Void> update(@PathVariable long menuId, @RequestBody UpdateMenuRequest req) {
    menuService.update(new MenuUpdateCommand(
        menuId,
        req.parentId(),
        req.name(),
        req.path(),
        req.icon(),
        req.sortOrder() == null ? 0 : req.sortOrder(),
        req.useYn() == null || req.useYn()
    ));
    return ApiResponse.ok();
  }

  @DeleteMapping("/{menuId}")
  public ApiResponse<Void> delete(@PathVariable long menuId) {
    menuService.delete(menuId);
    return ApiResponse.ok();
  }

  public record RoleSetRequest(List<String> roleKeys) {}

  @PutMapping("/{menuId}/roles")
  public ApiResponse<Void> setRoles(@PathVariable long menuId, @RequestBody RoleSetRequest req) {
    menuService.setRoles(menuId, req.roleKeys());
    return ApiResponse.ok();
  }
}

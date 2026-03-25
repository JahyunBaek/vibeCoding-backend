package com.example.commonsystem.menu.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.menu.dto.MenuCreateCommand;
import com.example.commonsystem.menu.dto.MenuNode;
import com.example.commonsystem.menu.dto.MenuUpdateCommand;
import com.example.commonsystem.menu.service.MenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 메뉴", description = "메뉴 관리")
@RestController
@RequestMapping("/api/admin/menus")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminMenuController {

  private final MenuService menuService;

  public AdminMenuController(MenuService menuService) {
    this.menuService = menuService;
  }

  @Operation(summary = "메뉴 트리 조회")
  @GetMapping("/tree")
  public ApiResponse<List<MenuNode>> tree(@RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(menuService.getAllMenuTree(tenantId));
  }

  public record CreateMenuRequest(
      Long parentId,
      @NotBlank @Size(max = 100) String name,
      @Size(max = 200) String path,
      @Size(max = 50) String icon,
      Integer sortOrder,
      Boolean useYn,
      String menuType,
      Long boardId,
      List<String> roleKeys,
      Long tenantId
  ) {}

  @Operation(summary = "메뉴 생성")
  @PostMapping
  public ApiResponse<Long> create(@Valid @RequestBody CreateMenuRequest req) {
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
      @NotBlank @Size(max = 100) String name,
      @Size(max = 200) String path,
      @Size(max = 50) String icon,
      Integer sortOrder,
      Boolean useYn
  ) {}

  @Operation(summary = "메뉴 수정")
  @PutMapping("/{menuId}")
  public ApiResponse<Void> update(@PathVariable long menuId, @Valid @RequestBody UpdateMenuRequest req) {
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

  @Operation(summary = "메뉴 삭제")
  @DeleteMapping("/{menuId}")
  public ApiResponse<Void> delete(@PathVariable long menuId) {
    menuService.delete(menuId);
    return ApiResponse.ok();
  }

  public record RoleSetRequest(List<String> roleKeys) {}

  @Operation(summary = "메뉴 역할 설정")
  @PutMapping("/{menuId}/roles")
  public ApiResponse<Void> setRoles(@PathVariable long menuId, @RequestBody RoleSetRequest req) {
    menuService.setRoles(menuId, req.roleKeys());
    return ApiResponse.ok();
  }
}

package com.example.commonsystem.menu.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.menu.dto.MenuNode;
import com.example.commonsystem.menu.service.MenuService;
import com.example.commonsystem.security.UserPrincipal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메뉴", description = "사용자 메뉴 조회")
@RestController
@RequestMapping("/api/menus")
public class MenuController {

  private final MenuService menuService;

  public MenuController(MenuService menuService) {
    this.menuService = menuService;
  }

  @Operation(summary = "내 메뉴 트리 조회")
  @GetMapping("/my")
  public ApiResponse<List<MenuNode>> myMenus(@AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(menuService.getMyMenuTree(principal.getRoleKey()));
  }
}

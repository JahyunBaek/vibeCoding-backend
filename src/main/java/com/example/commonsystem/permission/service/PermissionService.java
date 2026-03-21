package com.example.commonsystem.permission.service;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.permission.domain.Screen;
import com.example.commonsystem.permission.domain.ScreenAction;
import com.example.commonsystem.permission.dto.UserPermission;
import com.example.commonsystem.permission.mapper.PermissionMapper;
import com.example.commonsystem.security.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

  private final PermissionMapper permissionMapper;
  private final TenantContextHolder tenantCtx;

  public PermissionService(PermissionMapper permissionMapper, TenantContextHolder tenantCtx) {
    this.permissionMapper = permissionMapper;
    this.tenantCtx = tenantCtx;
  }

  /** 현재 유저의 권한 목록 (screenKey → actions) */
  public List<UserPermission> myPermissions() {
    // SUPER_ADMIN은 모든 권한 보유 — DB 조회 없이 전체 액션 반환
    if (tenantCtx.isSuperAdmin()) {
      List<ScreenAction> all = permissionMapper.findAllActions();
      Map<String, List<String>> grouped = all.stream()
          .collect(Collectors.groupingBy(
              ScreenAction::screenKey,
              Collectors.mapping(ScreenAction::actionKey, Collectors.toList())
          ));
      return grouped.entrySet().stream()
          .map(e -> new UserPermission(e.getKey(), e.getValue()))
          .collect(Collectors.toList());
    }
    String roleKey = currentRoleKey();
    List<ScreenAction> actions = permissionMapper.findActionsByRoleKey(roleKey, effectiveTenantId());
    Map<String, List<String>> grouped = actions.stream()
        .collect(Collectors.groupingBy(
            ScreenAction::screenKey,
            Collectors.mapping(ScreenAction::actionKey, Collectors.toList())
        ));
    return grouped.entrySet().stream()
        .map(e -> new UserPermission(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  /** AOP에서 호출: 현재 유저가 해당 action 권한이 있는지 확인 */
  public void checkAction(String screenKey, String actionKey) {
    // SUPER_ADMIN은 모든 액션 허용
    if (tenantCtx.isSuperAdmin()) return;
    String roleKey = currentRoleKey();
    if (!permissionMapper.hasPermission(roleKey, screenKey, actionKey, effectiveTenantId())) {
      throw new AppException(ErrorCode.FORBIDDEN, "해당 작업에 대한 권한이 없습니다.");
    }
  }

  // --- Admin: Screen CRUD ---
  public List<Screen> allScreens() {
    return permissionMapper.findAllScreens();
  }

  @Transactional
  public void createScreen(String screenKey, String screenName) {
    permissionMapper.insertScreen(new Screen(0, screenKey, screenName, true));
  }

  @Transactional
  public void updateScreen(int screenId, String screenName, boolean useYn) {
    permissionMapper.updateScreen(new Screen(screenId, null, screenName, useYn));
  }

  @Transactional
  public void deleteScreen(int screenId) {
    permissionMapper.deleteScreen(screenId);
  }

  // --- Admin: Action CRUD ---
  public List<ScreenAction> actionsByScreen(int screenId) {
    return permissionMapper.findActionsByScreen(screenId);
  }

  @Transactional
  public void createAction(int screenId, String actionKey, String actionName) {
    permissionMapper.insertAction(new ScreenAction(0, screenId, null, null, actionKey, actionName, true));
  }

  @Transactional
  public void updateAction(int actionId, String actionName, boolean useYn) {
    permissionMapper.updateAction(new ScreenAction(actionId, 0, null, null, null, actionName, useYn));
  }

  @Transactional
  public void deleteAction(int actionId) {
    permissionMapper.deleteAllRoleActionsByAction(actionId);
    permissionMapper.deleteAction(actionId);
  }

  // --- Admin: Role-Action Mapping ---
  public List<String> rolesByAction(int actionId) {
    return permissionMapper.findRoleKeysByAction(actionId, effectiveTenantId());
  }

  @Transactional
  public void setRoleActions(int actionId, List<String> roleKeys) {
    Long tenantId = effectiveTenantId();
    permissionMapper.deleteRoleActionsByAction(actionId, tenantId);
    for (String roleKey : roleKeys) {
      if ("SUPER_ADMIN".equals(roleKey) && !tenantCtx.isSuperAdmin()) continue;
      permissionMapper.insertRoleAction(roleKey, actionId, tenantId);
    }
  }

  /** SUPER_ADMIN은 시스템 테넌트(0)를 사용, 일반 사용자는 자신의 테넌트 ID */
  private Long effectiveTenantId() {
    Long tid = tenantCtx.currentTenantId();
    return tid != null ? tid : 0L;
  }

  private String currentRoleKey() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
      throw new AppException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return principal.getRoleKey();
  }
}

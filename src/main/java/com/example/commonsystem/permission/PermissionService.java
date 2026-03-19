package com.example.commonsystem.permission;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
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

  public PermissionService(PermissionMapper permissionMapper) {
    this.permissionMapper = permissionMapper;
  }

  /** 현재 유저의 권한 목록 (screenKey → actions) */
  public List<UserPermission> myPermissions() {
    String roleKey = currentRoleKey();
    List<ScreenAction> actions = permissionMapper.findActionsByRoleKey(roleKey);
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
    String roleKey = currentRoleKey();
    if (!permissionMapper.hasPermission(roleKey, screenKey, actionKey)) {
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
    permissionMapper.deleteRoleActionsByAction(actionId);
    permissionMapper.deleteAction(actionId);
  }

  // --- Admin: Role-Action Mapping ---
  public List<String> rolesByAction(int actionId) {
    return permissionMapper.findRoleKeysByAction(actionId);
  }

  @Transactional
  public void setRoleActions(int actionId, List<String> roleKeys) {
    permissionMapper.deleteRoleActionsByAction(actionId);
    for (String roleKey : roleKeys) {
      permissionMapper.insertRoleAction(roleKey, actionId);
    }
  }

  private String currentRoleKey() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
      throw new AppException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return principal.getRoleKey();
  }
}

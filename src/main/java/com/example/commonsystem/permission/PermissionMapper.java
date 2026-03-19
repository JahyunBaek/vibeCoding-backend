package com.example.commonsystem.permission;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PermissionMapper {
  // 내 권한 조회
  List<ScreenAction> findActionsByRoleKey(@Param("roleKey") String roleKey);

  // 화면 CRUD
  List<Screen> findAllScreens();
  void insertScreen(Screen screen);
  void updateScreen(Screen screen);
  void deleteScreen(@Param("screenId") int screenId);

  // 액션 CRUD
  List<ScreenAction> findActionsByScreen(@Param("screenId") int screenId);
  void insertAction(ScreenAction action);
  void updateAction(ScreenAction action);
  void deleteAction(@Param("actionId") int actionId);

  // 역할-액션 매핑
  List<String> findRoleKeysByAction(@Param("actionId") int actionId);
  void insertRoleAction(@Param("roleKey") String roleKey, @Param("actionId") int actionId);
  void deleteRoleAction(@Param("roleKey") String roleKey, @Param("actionId") int actionId);
  void deleteRoleActionsByAction(@Param("actionId") int actionId);

  // AOP용: 특정 userId의 roleKey로 특정 screen+action 허용 여부
  boolean hasPermission(@Param("roleKey") String roleKey, @Param("screenKey") String screenKey, @Param("actionKey") String actionKey);
}

package com.example.commonsystem.menu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.menu.domain.Menu;
import com.example.commonsystem.menu.dto.MenuCreateCommand;
import com.example.commonsystem.menu.dto.MenuUpdateCommand;

@Mapper
public interface MenuMapper {

  List<Menu> findByRole(@Param("roleKey") String roleKey, @Param("tenantId") Long tenantId);

  List<Menu> findAll(@Param("tenantId") Long tenantId);

  Menu findById(@Param("menuId") long menuId);

  Long findBoardsRootMenuId(@Param("tenantId") Long tenantId);

  Long findMaxSortOrder(@Param("parentId") Long parentId);

  void insert(MenuCreateCommand cmd);

  void update(MenuUpdateCommand cmd);

  void delete(@Param("menuId") long menuId);

  void deleteRoles(@Param("menuId") long menuId);

  void insertRole(@Param("menuId") long menuId, @Param("roleKey") String roleKey);

  List<String> findRoleKeysByMenuId(@Param("menuId") long menuId);

  Menu findByBoardId(@Param("boardId") long boardId);
}

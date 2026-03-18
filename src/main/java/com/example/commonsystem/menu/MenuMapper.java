package com.example.commonsystem.menu;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuMapper {

  List<Menu> findByRole(@Param("roleKey") String roleKey);

  List<Menu> findAll();

  Menu findById(@Param("menuId") long menuId);

  Long findBoardsRootMenuId();

  Long findMaxSortOrder(@Param("parentId") Long parentId);

  void insert(MenuCreateCommand cmd);

  void update(MenuUpdateCommand cmd);

  void delete(@Param("menuId") long menuId);

  void deleteRoles(@Param("menuId") long menuId);

  void insertRole(@Param("menuId") long menuId, @Param("roleKey") String roleKey);

  Menu findByBoardId(@Param("boardId") long boardId);
}

package com.example.commonsystem.tenant.mapper;

import com.example.commonsystem.tenant.domain.Tenant;
import com.example.commonsystem.tenant.dto.TenantCreateCommand;
import com.example.commonsystem.tenant.dto.TenantListRow;
import com.example.commonsystem.tenant.dto.TenantUpdateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TenantMapper {
  long count();
  List<TenantListRow> findPage(@Param("limit") int limit, @Param("offset") int offset);
  List<TenantListRow> findAll();
  Tenant findById(@Param("tenantId") long tenantId);

  void insert(TenantCreateCommand cmd);
  void update(TenantUpdateCommand cmd);
  void delete(@Param("tenantId") long tenantId);

  // Provisioning helpers
  void insertMenu(@Param("tenantId") long tenantId, @Param("menuId") long menuId,
      @Param("parentId") Long parentId, @Param("name") String name,
      @Param("path") String path, @Param("icon") String icon,
      @Param("sortOrder") int sortOrder, @Param("menuType") String menuType,
      @Param("boardId") Long boardId);
  void insertMenuRole(@Param("menuId") long menuId, @Param("roleKey") String roleKey);
  void insertBoard(@Param("tenantId") long tenantId, @Param("boardId") long boardId,
      @Param("name") String name, @Param("description") String description);
  void insertRoleActions(@Param("tenantId") long tenantId, @Param("roleKey") String roleKey);
  void insertCodeGroup(@Param("tenantId") long tenantId, @Param("groupKey") String groupKey,
      @Param("groupName") String groupName);
  void insertCode(@Param("tenantId") long tenantId, @Param("groupKey") String groupKey,
      @Param("code") String code, @Param("name") String name, @Param("value") String value,
      @Param("sortOrder") int sortOrder);
  void insertUser(@Param("tenantId") long tenantId, @Param("username") String username,
      @Param("passwordHash") String passwordHash, @Param("name") String name,
      @Param("roleKey") String roleKey);
  long getMenuIdSeqNext();
  long getBoardIdSeqNext();
}

package com.example.commonsystem.role.mapper;

import com.example.commonsystem.role.domain.Role;
import com.example.commonsystem.role.dto.RoleCreateCommand;
import com.example.commonsystem.role.dto.RoleUpdateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper {
  long count();
  List<Role> findPage(@Param("limit") int limit, @Param("offset") int offset);

  List<Role> findAll();

  void insert(RoleCreateCommand cmd);
  void update(RoleUpdateCommand cmd);
  void delete(@Param("roleKey") String roleKey);
}

package com.example.commonsystem.role;

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

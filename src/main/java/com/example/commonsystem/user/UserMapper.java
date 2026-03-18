package com.example.commonsystem.user;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  User findByUsername(@Param("username") String username);
  User findById(@Param("userId") long userId);

  long count(@Param("orgId") Long orgId);
  List<UserListRow> findPage(@Param("orgId") Long orgId, @Param("limit") int limit, @Param("offset") int offset);

  void insert(UserCreateCommand cmd);
  void update(UserUpdateCommand cmd);
  void delete(@Param("userId") long userId);
}

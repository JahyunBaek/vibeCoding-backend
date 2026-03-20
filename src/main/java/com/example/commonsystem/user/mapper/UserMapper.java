package com.example.commonsystem.user.mapper;

import com.example.commonsystem.user.domain.User;
import com.example.commonsystem.user.dto.UserCreateCommand;
import com.example.commonsystem.user.dto.UserListRow;
import com.example.commonsystem.user.dto.UserUpdateCommand;
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

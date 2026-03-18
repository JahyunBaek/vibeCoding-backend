package com.example.commonsystem.code;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeMapper {
  long countGroups();
  List<CodeGroup> findGroupPage(@Param("limit") int limit, @Param("offset") int offset);
  List<CodeGroup> findAllGroups();

  void insertGroup(CodeGroupCreateCommand cmd);
  void updateGroup(CodeGroupUpdateCommand cmd);
  void deleteGroup(@Param("groupKey") String groupKey);

  List<CodeItem> findCodesByGroup(@Param("groupKey") String groupKey);

  void insertCode(CodeCreateCommand cmd);
  void updateCode(CodeUpdateCommand cmd);
  void deleteCode(@Param("groupKey") String groupKey, @Param("code") String code);
}

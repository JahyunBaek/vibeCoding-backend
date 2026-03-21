package com.example.commonsystem.code.mapper;

import com.example.commonsystem.code.domain.CodeGroup;
import com.example.commonsystem.code.domain.CodeItem;
import com.example.commonsystem.code.dto.CodeCreateCommand;
import com.example.commonsystem.code.dto.CodeGroupCreateCommand;
import com.example.commonsystem.code.dto.CodeGroupUpdateCommand;
import com.example.commonsystem.code.dto.CodeUpdateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeMapper {
  long countGroups(@Param("tenantId") Long tenantId);
  List<CodeGroup> findGroupPage(@Param("tenantId") Long tenantId,
      @Param("limit") int limit, @Param("offset") int offset);
  List<CodeGroup> findAllGroups(@Param("tenantId") Long tenantId);

  void insertGroup(CodeGroupCreateCommand cmd);
  void updateGroup(CodeGroupUpdateCommand cmd);
  void deleteGroup(@Param("tenantId") Long tenantId, @Param("groupKey") String groupKey);

  List<CodeItem> findCodesByGroup(@Param("tenantId") Long tenantId,
      @Param("groupKey") String groupKey);

  void insertCode(CodeCreateCommand cmd);
  void updateCode(CodeUpdateCommand cmd);
  void deleteCode(@Param("tenantId") Long tenantId,
      @Param("groupKey") String groupKey, @Param("code") String code);
}

package com.example.commonsystem.org.mapper;

import com.example.commonsystem.org.domain.Org;
import com.example.commonsystem.org.dto.OrgCreateCommand;
import com.example.commonsystem.org.dto.OrgUpdateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrgMapper {
  List<Org> findAll();

  long count();
  List<Org> findPage(@Param("limit") int limit, @Param("offset") int offset);

  void insert(OrgCreateCommand cmd);
  void update(OrgUpdateCommand cmd);
  void delete(@Param("orgId") long orgId);
}

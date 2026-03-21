package com.example.commonsystem.audit.mapper;

import com.example.commonsystem.audit.domain.AuditLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper {

  void insert(
      @Param("tenantId")   Long tenantId,
      @Param("userId")     Long userId,
      @Param("username")   String username,
      @Param("action")     String action,
      @Param("targetType") String targetType,
      @Param("targetId")   String targetId,
      @Param("detail")     String detail,
      @Param("ipAddress")  String ipAddress
  );

  long count(
      @Param("tenantId")   Long tenantId,
      @Param("action")     String action,
      @Param("targetType") String targetType
  );

  List<AuditLog> findPage(
      @Param("tenantId")   Long tenantId,
      @Param("action")     String action,
      @Param("targetType") String targetType,
      @Param("limit")      int limit,
      @Param("offset")     int offset
  );
}

package com.example.commonsystem.tenant.mapper;

import com.example.commonsystem.tenant.domain.TenantConfig;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TenantConfigMapper {
  List<TenantConfig> findByTenantId(@Param("tenantId") long tenantId);
  void upsert(@Param("tenantId") long tenantId, @Param("configKey") String configKey,
      @Param("configValue") String configValue);
  void deleteByTenantId(@Param("tenantId") long tenantId);
}

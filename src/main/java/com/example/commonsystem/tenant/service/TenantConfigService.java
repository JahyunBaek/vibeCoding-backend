package com.example.commonsystem.tenant.service;

import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.tenant.domain.TenantConfig;
import com.example.commonsystem.tenant.mapper.TenantConfigMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantConfigService {

  private final TenantConfigMapper configMapper;
  private final TenantContextHolder tenantCtx;

  public TenantConfigService(TenantConfigMapper configMapper, TenantContextHolder tenantCtx) {
    this.configMapper = configMapper;
    this.tenantCtx = tenantCtx;
  }

  public List<TenantConfig> getAll(Long tenantIdOverride) {
    long tid = effectiveTenantId(tenantIdOverride);
    return configMapper.findByTenantId(tid);
  }

  @Transactional
  public void saveAll(Map<String, String> configs, Long tenantIdOverride) {
    long tid = effectiveTenantId(tenantIdOverride);
    for (Map.Entry<String, String> entry : configs.entrySet()) {
      configMapper.upsert(tid, entry.getKey(), entry.getValue());
    }
  }

  /** provisioning 시 기본값 삽입 */
  @Transactional
  public void initDefaults(long tenantId, String tenantName) {
    configMapper.upsert(tenantId, "company_name", tenantName);
    configMapper.upsert(tenantId, "logo_url", "");
    configMapper.upsert(tenantId, "timezone", "Asia/Seoul");
    configMapper.upsert(tenantId, "locale", "ko");
  }

  private long effectiveTenantId(Long override) {
    Long resolved = tenantCtx.resolveTenantId(override);
    return resolved != null ? resolved : 0L;
  }
}

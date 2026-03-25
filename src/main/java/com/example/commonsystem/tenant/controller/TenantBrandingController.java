package com.example.commonsystem.tenant.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.tenant.domain.TenantConfig;
import com.example.commonsystem.tenant.mapper.TenantConfigMapper;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public branding endpoint accessible by any authenticated user.
 * Returns company_name and logo_url for the current user's tenant.
 */
@Tag(name = "테넌트 브랜딩", description = "테넌트 브랜딩 정보 조회")
@RestController
@RequestMapping("/api/tenant")
public class TenantBrandingController {

  private final TenantConfigMapper configMapper;
  private final TenantContextHolder tenantCtx;

  public TenantBrandingController(TenantConfigMapper configMapper, TenantContextHolder tenantCtx) {
    this.configMapper = configMapper;
    this.tenantCtx = tenantCtx;
  }

  @Operation(summary = "브랜딩 정보 조회")
  @GetMapping("/branding")
  public ApiResponse<Map<String, String>> getBranding() {
    Long tenantId = tenantCtx.currentTenantId();
    if (tenantId == null) {
      // SUPER_ADMIN has no tenant — return defaults
      return ApiResponse.ok(Map.of("companyName", "Common System", "logoUrl", ""));
    }

    List<TenantConfig> configs = configMapper.findByTenantId(tenantId);

    String companyName = "Common System";
    String logoUrl = "";

    for (TenantConfig c : configs) {
      if ("company_name".equals(c.configKey())) {
        companyName = c.configValue() != null ? c.configValue() : companyName;
      } else if ("logo_url".equals(c.configKey())) {
        logoUrl = c.configValue() != null ? c.configValue() : "";
      }
    }

    return ApiResponse.ok(Map.of("companyName", companyName, "logoUrl", logoUrl));
  }
}

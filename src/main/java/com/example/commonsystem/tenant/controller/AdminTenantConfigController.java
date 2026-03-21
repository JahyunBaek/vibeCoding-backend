package com.example.commonsystem.tenant.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.tenant.domain.TenantConfig;
import com.example.commonsystem.tenant.service.TenantConfigService;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminTenantConfigController {

  private final TenantConfigService configService;

  public AdminTenantConfigController(TenantConfigService configService) {
    this.configService = configService;
  }

  @GetMapping
  public ApiResponse<List<TenantConfig>> get(@RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(configService.getAll(tenantId));
  }

  @PutMapping
  public ApiResponse<Void> save(@RequestBody Map<String, String> configs,
      @RequestParam(required = false) Long tenantId) {
    configService.saveAll(configs, tenantId);
    return ApiResponse.ok();
  }
}

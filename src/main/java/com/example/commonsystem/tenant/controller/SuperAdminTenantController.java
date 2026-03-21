package com.example.commonsystem.tenant.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.tenant.domain.Tenant;
import com.example.commonsystem.tenant.dto.TenantCreateResult;
import com.example.commonsystem.tenant.dto.TenantListRow;
import com.example.commonsystem.tenant.service.TenantService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminTenantController {

  private final TenantService tenantService;

  public SuperAdminTenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @GetMapping
  public ApiResponse<PageResponse<TenantListRow>> list(
      @RequestParam(defaultValue = "1")  int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(tenantService.page(page, size));
  }

  @GetMapping("/all")
  public ApiResponse<List<TenantListRow>> all() {
    return ApiResponse.ok(tenantService.all());
  }

  @GetMapping("/{tenantId}")
  public ApiResponse<Tenant> get(@PathVariable long tenantId) {
    return ApiResponse.ok(tenantService.findById(tenantId));
  }

  public record CreateTenantRequest(
      @NotBlank String tenantKey,
      @NotBlank String tenantName,
      String planType,
      @NotBlank String adminUsername,
      @NotBlank String adminPassword
  ) {}

  @PostMapping
  public ApiResponse<TenantCreateResult> create(@RequestBody CreateTenantRequest req) {
    TenantCreateResult result = tenantService.create(
        req.tenantKey(), req.tenantName(),
        req.planType() != null ? req.planType() : "BASIC",
        req.adminUsername(), req.adminPassword()
    );
    return ApiResponse.ok(result);
  }

  public record UpdateTenantRequest(
      @NotBlank String tenantName,
      String planType,
      Boolean active
  ) {}

  @PutMapping("/{tenantId}")
  public ApiResponse<Void> update(@PathVariable long tenantId, @RequestBody UpdateTenantRequest req) {
    tenantService.update(tenantId, req.tenantName(),
        req.planType() != null ? req.planType() : "BASIC",
        req.active() == null || req.active());
    return ApiResponse.ok();
  }

  @DeleteMapping("/{tenantId}")
  public ApiResponse<Void> delete(@PathVariable long tenantId) {
    tenantService.delete(tenantId);
    return ApiResponse.ok();
  }
}

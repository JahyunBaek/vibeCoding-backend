package com.example.commonsystem.tenant.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.tenant.domain.Tenant;
import com.example.commonsystem.tenant.dto.TenantCreateResult;
import com.example.commonsystem.tenant.dto.TenantListRow;
import com.example.commonsystem.tenant.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "슈퍼관리자 - 테넌트", description = "테넌트 관리")
@RestController
@RequestMapping("/api/super-admin/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminTenantController {

  private final TenantService tenantService;

  public SuperAdminTenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @Operation(summary = "테넌트 목록 조회")
  @GetMapping
  public ApiResponse<PageResponse<TenantListRow>> list(
      @RequestParam(defaultValue = "1")  int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(tenantService.page(page, size));
  }

  @Operation(summary = "전체 테넌트 조회")
  @GetMapping("/all")
  public ApiResponse<List<TenantListRow>> all() {
    return ApiResponse.ok(tenantService.all());
  }

  @Operation(summary = "테넌트 상세 조회")
  @GetMapping("/{tenantId}")
  public ApiResponse<Tenant> get(@PathVariable long tenantId) {
    return ApiResponse.ok(tenantService.findById(tenantId));
  }

  public record CreateTenantRequest(
      @NotBlank String tenantKey,
      @NotBlank String tenantName,
      String planType,
      @NotBlank @Size(min = 3, max = 50) String adminUsername,
      @NotBlank @Size(min = 8, max = 100) String adminPassword
  ) {}

  @Operation(summary = "테넌트 생성")
  @PostMapping
  public ApiResponse<TenantCreateResult> create(@Valid @RequestBody CreateTenantRequest req) {
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

  @Operation(summary = "테넌트 수정")
  @PutMapping("/{tenantId}")
  public ApiResponse<Void> update(@PathVariable long tenantId, @Valid @RequestBody UpdateTenantRequest req) {
    tenantService.update(tenantId, req.tenantName(),
        req.planType() != null ? req.planType() : "BASIC",
        req.active() == null || req.active());
    return ApiResponse.ok();
  }

  @Operation(summary = "테넌트 삭제")
  @DeleteMapping("/{tenantId}")
  public ApiResponse<Void> delete(@PathVariable long tenantId) {
    tenantService.delete(tenantId);
    return ApiResponse.ok();
  }
}

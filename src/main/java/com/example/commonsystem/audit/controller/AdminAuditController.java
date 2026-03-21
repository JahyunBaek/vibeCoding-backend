package com.example.commonsystem.audit.controller;

import com.example.commonsystem.audit.domain.AuditLog;
import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminAuditController {

  private final AuditService auditService;

  public AdminAuditController(AuditService auditService) {
    this.auditService = auditService;
  }

  @GetMapping
  public ApiResponse<PageResponse<AuditLog>> list(
      @RequestParam(required = false) Long tenantId,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String targetType,
      @RequestParam(defaultValue = "1")  int page,
      @RequestParam(defaultValue = "50") int size
  ) {
    return ApiResponse.ok(auditService.page(tenantId, action, targetType, page, size));
  }
}

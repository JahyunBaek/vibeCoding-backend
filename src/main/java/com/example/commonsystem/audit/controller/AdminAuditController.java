package com.example.commonsystem.audit.controller;

import com.example.commonsystem.audit.domain.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.CsvExportService;
import com.example.commonsystem.common.I18nService;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.tenant.service.TenantConfigService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 감사로그", description = "감사 로그 조회")
@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminAuditController {

  private final AuditService auditService;
  private final CsvExportService csvExportService;
  private final I18nService i18n;
  private final TenantContextHolder tenantCtx;
  private final TenantConfigService tenantConfigService;

  public AdminAuditController(AuditService auditService, CsvExportService csvExportService,
      I18nService i18n, TenantContextHolder tenantCtx,
      TenantConfigService tenantConfigService) {
    this.auditService = auditService;
    this.csvExportService = csvExportService;
    this.i18n = i18n;
    this.tenantCtx = tenantCtx;
    this.tenantConfigService = tenantConfigService;
  }

  @Operation(summary = "감사로그 목록 조회")
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

  @Operation(summary = "감사로그 CSV 내보내기")
  @GetMapping("/export")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) Long tenantId,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String targetType
  ) {
    List<AuditLog> logs = auditService.listAll(tenantId, action, targetType);
    String locale = tenantConfigService.getLocale(tenantCtx.currentTenantId());
    List<String> headers = List.of(
        i18n.getMessage("csv.audit.logId", locale),
        i18n.getMessage("csv.audit.user", locale),
        i18n.getMessage("csv.audit.action", locale),
        i18n.getMessage("csv.audit.targetType", locale),
        i18n.getMessage("csv.audit.targetId", locale),
        i18n.getMessage("csv.audit.detail", locale),
        i18n.getMessage("csv.audit.dateTime", locale)
    );
    List<List<String>> rows = new ArrayList<>();
    for (AuditLog log : logs) {
      rows.add(List.of(
          String.valueOf(log.logId()),
          log.username() != null ? log.username() : "",
          log.action() != null ? log.action() : "",
          log.targetType() != null ? log.targetType() : "",
          log.targetId() != null ? log.targetId() : "",
          log.detail() != null ? log.detail() : "",
          log.createdAt() != null ? log.createdAt() : ""
      ));
    }
    String csv = csvExportService.toCsv(headers, rows);
    byte[] body = csv.getBytes(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(body);
  }
}

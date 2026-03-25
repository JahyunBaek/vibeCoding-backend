package com.example.commonsystem.audit.controller;

import com.example.commonsystem.audit.domain.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.CsvExportService;
import com.example.commonsystem.common.PageResponse;
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

  public AdminAuditController(AuditService auditService, CsvExportService csvExportService) {
    this.auditService = auditService;
    this.csvExportService = csvExportService;
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
    List<String> headers = List.of("로그ID", "사용자", "액션", "대상타입", "대상ID", "상세", "일시");
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

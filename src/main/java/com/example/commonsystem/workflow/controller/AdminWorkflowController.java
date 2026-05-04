package com.example.commonsystem.workflow.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.workflow.dto.WorkflowDtos.UpdateWorkflowRequest;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowDetail;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowListRow;
import com.example.commonsystem.workflow.service.WorkflowDefinitionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 테넌트 워크플로우 관리 (ADMIN).
 * - 시스템 템플릿을 자기 테넌트로 복사 (Copy-on-Customize)
 * - 자기 테넌트의 워크플로우 정의 수정 (정책: 권한/결재/알림/스킵)
 * - 구조 변경(상태 코드/액션 코드 추가)은 시스템 템플릿 단계에서만 (시드 무결성 보호)
 */
@Tag(name = "워크플로우 관리 (ADMIN)", description = "테넌트 워크플로우 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/workflow")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminWorkflowController {

  private final WorkflowDefinitionService service;

  @Operation(summary = "내 테넌트 워크플로우 목록")
  @GetMapping
  public ApiResponse<List<WorkflowListRow>> list(
      @RequestParam(required = false) String entityType,
      @RequestParam(defaultValue = "false") boolean activeOnly) {
    return ApiResponse.ok(service.listForTenant(entityType, activeOnly));
  }

  @Operation(summary = "워크플로우 상세")
  @GetMapping("/{workflowId}")
  public ApiResponse<WorkflowDetail> detail(@PathVariable long workflowId) {
    return ApiResponse.ok(service.detail(workflowId));
  }

  @Operation(summary = "시스템 템플릿을 내 테넌트로 복사")
  @PostMapping("/copy-from-template/{templateWorkflowId}")
  public ApiResponse<Long> copyFromTemplate(@PathVariable long templateWorkflowId,
                                             @AuthenticationPrincipal UserPrincipal user) {
    return ApiResponse.ok(service.copyTemplateToTenant(templateWorkflowId, user.getUserId()));
  }

  @Operation(summary = "테넌트 워크플로우 수정 (정책 변경)")
  @PutMapping("/{workflowId}")
  public ApiResponse<Void> update(@PathVariable long workflowId,
                                   @Valid @RequestBody UpdateWorkflowRequest req) {
    service.updateTenant(workflowId, req);
    return ApiResponse.ok();
  }

  @Operation(summary = "테넌트 워크플로우 삭제")
  @DeleteMapping("/{workflowId}")
  public ApiResponse<Void> delete(@PathVariable long workflowId) {
    service.delete(workflowId, false);
    return ApiResponse.ok();
  }
}

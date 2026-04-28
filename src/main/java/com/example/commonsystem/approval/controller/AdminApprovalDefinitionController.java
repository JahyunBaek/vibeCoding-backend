package com.example.commonsystem.approval.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateAuthorityRuleRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequiredStepRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionDetail;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionListRow;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.UpdateRequest;
import com.example.commonsystem.approval.service.ApprovalDefinitionService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "결재 정책 (관리자)", description = "결재 기능 코드/정책 관리")
@RestController
@RequestMapping("/api/admin/approval/definitions")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminApprovalDefinitionController {

  private final ApprovalDefinitionService service;

  public AdminApprovalDefinitionController(ApprovalDefinitionService service) {
    this.service = service;
  }

  @Operation(summary = "결재 정책 목록")
  @GetMapping
  public ApiResponse<List<DefinitionListRow>> list(
      @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(service.list(activeOnly, keyword, tenantId));
  }

  @Operation(summary = "결재 정책 상세")
  @GetMapping("/{definitionId}")
  public ApiResponse<DefinitionDetail> get(
      @PathVariable long definitionId,
      @RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(service.get(definitionId, tenantId));
  }

  @Operation(summary = "결재 정책 생성")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "CREATE")
  @PostMapping
  public ApiResponse<Long> create(
      @Valid @RequestBody CreateRequest req,
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(service.create(req, principal.getUserId(), tenantId));
  }

  @Operation(summary = "결재 정책 수정")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "EDIT")
  @PutMapping("/{definitionId}")
  public ApiResponse<Void> update(
      @PathVariable long definitionId,
      @Valid @RequestBody UpdateRequest req,
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(required = false) Long tenantId) {
    service.update(definitionId, req, principal.getUserId(), tenantId);
    return ApiResponse.ok();
  }

  @Operation(summary = "결재 정책 삭제")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "DELETE")
  @DeleteMapping("/{definitionId}")
  public ApiResponse<Void> delete(
      @PathVariable long definitionId,
      @RequestParam(required = false) Long tenantId) {
    service.delete(definitionId, tenantId);
    return ApiResponse.ok();
  }

  @Operation(summary = "결재 권한 규칙 추가")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "MANAGE")
  @PostMapping("/{approvalCode}/authorities")
  public ApiResponse<Void> addAuthorityRule(
      @PathVariable String approvalCode,
      @Valid @RequestBody CreateAuthorityRuleRequest req,
      @RequestParam(required = false) Long tenantId) {
    service.addAuthorityRule(approvalCode, req, tenantId);
    return ApiResponse.ok();
  }

  @Operation(summary = "결재 권한 규칙 삭제")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "MANAGE")
  @DeleteMapping("/authorities/{ruleId}")
  public ApiResponse<Void> deleteAuthorityRule(
      @PathVariable long ruleId,
      @RequestParam(required = false) Long tenantId) {
    service.deleteAuthorityRule(ruleId, tenantId);
    return ApiResponse.ok();
  }

  @Operation(summary = "필수 결재 단계 추가",
      description = "사용자 결재선 뒤에 자동으로 강제 추가되는 단계")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "MANAGE")
  @PostMapping("/{definitionId}/required-steps")
  public ApiResponse<Void> addRequiredStep(
      @PathVariable long definitionId,
      @Valid @RequestBody CreateRequiredStepRequest req,
      @RequestParam(required = false) Long tenantId) {
    service.addRequiredStep(definitionId, req, tenantId);
    return ApiResponse.ok();
  }

  @Operation(summary = "필수 결재 단계 삭제")
  @RequiresAction(screen = "ADMIN_APPROVAL_DEF", action = "MANAGE")
  @DeleteMapping("/{definitionId}/required-steps/{requiredStepId}")
  public ApiResponse<Void> deleteRequiredStep(
      @PathVariable long definitionId,
      @PathVariable long requiredStepId,
      @RequestParam(required = false) Long tenantId) {
    service.deleteRequiredStep(definitionId, requiredStepId, tenantId);
    return ApiResponse.ok();
  }
}

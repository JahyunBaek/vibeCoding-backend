package com.example.commonsystem.workflow.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.workflow.dto.WorkflowDtos.CreateWorkflowRequest;
import com.example.commonsystem.workflow.dto.WorkflowDtos.StateRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.UpdateWorkflowRequest;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowDetail;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowListRow;
import com.example.commonsystem.workflow.service.WorkflowDefinitionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 시스템 워크플로우 템플릿 관리 (SUPER_ADMIN 전용).
 * - 모든 테넌트가 새로 프로비저닝될 때 복사받는 baseline 템플릿
 * - 글로벌 상태 풀(workflow_states) 조회
 */
@Tag(name = "워크플로우 템플릿 (SUPER_ADMIN)", description = "시스템 워크플로우 템플릿 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/super-admin/workflow")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminWorkflowTemplateController {

  private final WorkflowDefinitionService service;

  @Operation(summary = "글로벌 상태 풀 조회")
  @GetMapping("/states")
  public ApiResponse<List<StateRow>> states() {
    return ApiResponse.ok(service.allStates());
  }

  @Operation(summary = "시스템 템플릿 목록")
  @GetMapping("/templates")
  public ApiResponse<List<WorkflowListRow>> listTemplates() {
    return ApiResponse.ok(service.listTemplates());
  }

  @Operation(summary = "템플릿 상세")
  @GetMapping("/templates/{workflowId}")
  public ApiResponse<WorkflowDetail> detail(@PathVariable long workflowId) {
    return ApiResponse.ok(service.detail(workflowId));
  }

  @Operation(summary = "템플릿 신규 등록")
  @PostMapping("/templates")
  public ApiResponse<Long> create(@Valid @RequestBody CreateWorkflowRequest req,
                                   @AuthenticationPrincipal UserPrincipal user) {
    return ApiResponse.ok(service.createTemplate(req, user.getUserId()));
  }

  @Operation(summary = "템플릿 수정")
  @PutMapping("/templates/{workflowId}")
  public ApiResponse<Void> update(@PathVariable long workflowId,
                                   @Valid @RequestBody UpdateWorkflowRequest req) {
    service.updateTemplate(workflowId, req);
    return ApiResponse.ok();
  }

  @Operation(summary = "템플릿 삭제")
  @DeleteMapping("/templates/{workflowId}")
  public ApiResponse<Void> delete(@PathVariable long workflowId) {
    service.delete(workflowId, true);
    return ApiResponse.ok();
  }
}

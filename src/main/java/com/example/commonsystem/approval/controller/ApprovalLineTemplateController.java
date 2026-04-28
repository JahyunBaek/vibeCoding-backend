package com.example.commonsystem.approval.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.CreateRequest;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateDetail;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateListRow;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.UpdateRequest;
import com.example.commonsystem.approval.service.ApprovalLineTemplateService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "내 결재선 양식", description = "개인 결재선 양식 CRUD")
@RestController
@RequestMapping("/api/approval/lines")
public class ApprovalLineTemplateController {

  private final ApprovalLineTemplateService service;

  public ApprovalLineTemplateController(ApprovalLineTemplateService service) {
    this.service = service;
  }

  @Operation(summary = "내 결재선 양식 목록")
  @GetMapping
  public ApiResponse<List<TemplateListRow>> list(
      @RequestParam(required = false) String approvalCode,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(service.myTemplates(principal.getUserId(), approvalCode));
  }

  @Operation(summary = "내 결재선 양식 상세")
  @GetMapping("/{templateId}")
  public ApiResponse<TemplateDetail> detail(
      @PathVariable long templateId,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(service.detail(templateId, principal.getUserId()));
  }

  @Operation(summary = "결재선 양식 생성")
  @RequiresAction(screen = "MY_APPROVAL_LINE", action = "CREATE")
  @PostMapping
  public ApiResponse<Long> create(
      @Valid @RequestBody CreateRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(service.create(req, principal.getUserId()));
  }

  @Operation(summary = "결재선 양식 수정")
  @RequiresAction(screen = "MY_APPROVAL_LINE", action = "EDIT")
  @PutMapping("/{templateId}")
  public ApiResponse<Void> update(
      @PathVariable long templateId,
      @Valid @RequestBody UpdateRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    service.update(templateId, req, principal.getUserId());
    return ApiResponse.ok();
  }

  @Operation(summary = "결재선 양식 삭제")
  @RequiresAction(screen = "MY_APPROVAL_LINE", action = "DELETE")
  @DeleteMapping("/{templateId}")
  public ApiResponse<Void> delete(
      @PathVariable long templateId,
      @AuthenticationPrincipal UserPrincipal principal) {
    service.delete(templateId, principal.getUserId());
    return ApiResponse.ok();
  }
}

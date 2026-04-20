package com.example.commonsystem.approval.controller;

import java.time.Instant;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.ActionRequest;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentDetail;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentListRow;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.ListQuery;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.PopupInitResponse;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.RequestApprovalRequest;
import com.example.commonsystem.approval.service.ApprovalDocumentService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "결재 문서", description = "공통 결재 요청/승인/반려/조회")
@RestController
@RequestMapping("/api/approval/documents")
public class ApprovalDocumentController {

  private final ApprovalDocumentService service;

  public ApprovalDocumentController(ApprovalDocumentService service) {
    this.service = service;
  }

  @Operation(summary = "공통 결재 팝업 초기 데이터 (정책 + 기본 양식 + 양식 목록)")
  @GetMapping("/popup-init")
  public ApiResponse<PopupInitResponse> popupInit(
      @RequestParam String approvalCode,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(service.popupInit(approvalCode, principal));
  }

  @Operation(summary = "결재 요청 (상신)")
  @RequiresAction(screen = "APPROVAL_DOCUMENT", action = "REQUEST")
  @PostMapping
  public ApiResponse<Long> request(
      @Valid @RequestBody RequestApprovalRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ApiResponse.ok(service.request(req, principal));
  }

  @Operation(summary = "결재 문서 상세")
  @GetMapping("/{documentId}")
  public ApiResponse<DocumentDetail> detail(@PathVariable long documentId) {
    return ApiResponse.ok(service.detail(documentId));
  }

  @Operation(summary = "공통 결재함 목록")
  @GetMapping
  public ApiResponse<PageResponse<DocumentListRow>> list(
      @RequestParam(required = false, defaultValue = "requested") String inbox,
      @RequestParam(required = false) String approvalCode,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Instant fromDate,
      @RequestParam(required = false) Instant toDate,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal UserPrincipal principal) {
    ListQuery q = new ListQuery(inbox, approvalCode, status, keyword, fromDate, toDate, page, size);
    return ApiResponse.ok(service.list(q, principal));
  }

  @Operation(summary = "결재 승인")
  @RequiresAction(screen = "APPROVAL_DOCUMENT", action = "APPROVE")
  @PostMapping("/{documentId}/steps/{stepId}/approve")
  public ApiResponse<Void> approve(
      @PathVariable long documentId,
      @PathVariable long stepId,
      @Valid @RequestBody(required = false) ActionRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    service.approve(documentId, stepId, req != null ? req : new ActionRequest(null), principal);
    return ApiResponse.ok();
  }

  @Operation(summary = "결재 반려")
  @RequiresAction(screen = "APPROVAL_DOCUMENT", action = "REJECT")
  @PostMapping("/{documentId}/steps/{stepId}/reject")
  public ApiResponse<Void> reject(
      @PathVariable long documentId,
      @PathVariable long stepId,
      @Valid @RequestBody(required = false) ActionRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    service.reject(documentId, stepId, req != null ? req : new ActionRequest(null), principal);
    return ApiResponse.ok();
  }

  @Operation(summary = "결재 회수 (요청자 본인)")
  @RequiresAction(screen = "APPROVAL_DOCUMENT", action = "WITHDRAW")
  @PostMapping("/{documentId}/withdraw")
  public ApiResponse<Void> withdraw(
      @PathVariable long documentId,
      @AuthenticationPrincipal UserPrincipal principal) {
    service.withdraw(documentId, principal);
    return ApiResponse.ok();
  }
}

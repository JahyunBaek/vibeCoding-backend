package com.example.commonsystem.approval.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRequest;

/** 결재 문서 DTO */
public class ApprovalDocumentDtos {

  /** 목록 행 — 공통 결재함 */
  public record DocumentListRow(
      long documentId,
      long tenantId,
      String documentNo,
      String approvalCode,
      String approvalName,
      String title,
      long requesterUserId,
      String requesterName,
      Long requesterDepartmentId,
      String requesterDepartmentName,
      Long supervisingDepartmentId,
      String supervisingDepartmentName,
      String status,
      Integer currentStepOrder,
      String currentStepName,
      Instant requestedAt,
      Instant completedAt
  ) {}

  /** 문서 상세 */
  public record DocumentDetail(
      long documentId,
      long tenantId,
      String documentNo,
      String approvalCode,
      String approvalName,
      String businessType,
      String businessId,
      String title,
      String body,
      long requesterUserId,
      String requesterName,
      Long requesterDepartmentId,
      String requesterDepartmentName,
      Long supervisingDepartmentId,
      String supervisingDepartmentName,
      String status,
      Integer currentStepOrder,
      Instant requestedAt,
      Instant completedAt,
      List<DocumentStepRow> steps,
      List<HistoryRow> history
  ) {}

  public record DocumentStepRow(
      long stepId,
      int stepOrder,
      String stepName,
      String approvalType,
      String targetDepartmentType,
      Long targetDepartmentId,
      String targetDepartmentName,
      String targetRoleKey,
      boolean groupApprovalYn,
      String status,
      Long actedByUserId,
      String actedByName,
      Instant actedAt,
      String comment
  ) {}

  public record HistoryRow(
      long historyId,
      Long stepId,
      String actionType,
      long actionBy,
      String actionByName,
      String actionComment,
      Instant actionAt,
      String beforeStatus,
      String afterStatus
  ) {}

  /** 결재 요청 (상신) */
  public record RequestApprovalRequest(
      @NotBlank @Size(max = 80) String approvalCode,
      String businessType,
      String businessId,
      @NotBlank @Size(max = 300) String title,
      String body,
      Long supervisingDepartmentId,   // 기본값이 있으면 생략 가능
      Long templateId,                // 저장된 양식 사용 시
      @Valid List<StepRequest> steps  // 직접 구성 시 (양식 미사용)
  ) {}

  /** 승인/반려 액션 */
  public record ActionRequest(
      @Size(max = 1000) String comment
  ) {}

  /** 목록 검색 파라미터 */
  public record ListQuery(
      String inbox,           // requested | pending | processed | all
      String approvalCode,
      String status,
      String keyword,
      Instant fromDate,
      Instant toDate,
      int page,
      int size
  ) {}

  /** 팝업 초기화용: approval_code 로 정책 + 기본 양식 조회 */
  public record PopupInitResponse(
      ApprovalDefinitionDtos.DefinitionDetail definition,
      ApprovalLineTemplateDtos.TemplateDetail defaultTemplate,
      List<ApprovalLineTemplateDtos.TemplateListRow> templates,
      @NotEmpty List<ApprovalLineTemplateDtos.StepRow> previewSteps
  ) {}
}

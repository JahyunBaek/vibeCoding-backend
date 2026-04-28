package com.example.commonsystem.approval.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** 개인 결재선 양식 DTO */
public class ApprovalLineTemplateDtos {

  /** 목록 행 */
  public record TemplateListRow(
      long templateId,
      String approvalCode,
      String templateName,
      boolean defaultYn,
      boolean activeYn,
      int stepCount,
      Instant updatedAt
  ) {}

  /** 상세 */
  public record TemplateDetail(
      long templateId,
      long tenantId,
      long ownerUserId,
      String approvalCode,
      String templateName,
      boolean defaultYn,
      boolean activeYn,
      List<StepRow> steps
  ) {}

  public record StepRow(
      Long stepId,
      int stepOrder,
      String stepName,
      String approvalType,
      String targetDepartmentType,
      Long targetDepartmentId,
      String targetDepartmentName,
      String targetRoleKey,
      Long targetUserId,
      String targetUserName,
      boolean groupApprovalYn,
      boolean requiredYn
  ) {}

  /** 단계 요청 (생성/수정 공용) */
  public record StepRequest(
      int stepOrder,
      @NotBlank @Size(max = 100) String stepName,
      String approvalType,         // APPROVE (기본)
      @NotBlank String targetDepartmentType, // REQUEST | SUPERVISING | CUSTOM | USER
      Long targetDepartmentId,
      String targetRoleKey,
      Long targetUserId,           // USER 타입일 때 필수
      Boolean groupApprovalYn,
      Boolean requiredYn
  ) {}

  public record CreateRequest(
      @NotBlank @Size(max = 80) String approvalCode,
      @NotBlank @Size(max = 200) String templateName,
      boolean defaultYn,
      @NotEmpty @Valid List<StepRequest> steps
  ) {}

  public record UpdateRequest(
      @NotBlank @Size(max = 200) String templateName,
      boolean defaultYn,
      boolean activeYn,
      @NotEmpty @Valid List<StepRequest> steps
  ) {}
}

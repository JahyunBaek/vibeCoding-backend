package com.example.commonsystem.approval.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 결재 기능 코드 마스터 관련 DTO */
public class ApprovalDefinitionDtos {

  /** 목록 행 */
  public record DefinitionListRow(
      long definitionId,
      long tenantId,
      String approvalCode,
      String approvalName,
      String description,
      boolean useRequestDepartment,
      boolean useSupervisingDepartment,
      Long defaultSupervisingDepartmentId,
      String defaultSupervisingDepartmentName,
      boolean useGroupApproval,
      boolean usePersonalLineTemplate,
      boolean activeYn,
      int sortOrder,
      Instant createdAt,
      Instant updatedAt
  ) {}

  /** 상세 (+ 권한 규칙 목록) */
  public record DefinitionDetail(
      long definitionId,
      long tenantId,
      String approvalCode,
      String approvalName,
      String description,
      boolean useRequestDepartment,
      boolean useSupervisingDepartment,
      Long defaultSupervisingDepartmentId,
      boolean useGroupApproval,
      boolean usePersonalLineTemplate,
      boolean activeYn,
      int sortOrder,
      String remark,
      List<AuthorityRuleRow> authorityRules
  ) {}

  public record AuthorityRuleRow(
      Long ruleId,
      String approvalCode,
      Long targetDepartmentId,
      String targetDepartmentName,
      String targetRoleKey,
      String stepType,
      boolean activeYn
  ) {}

  /** 생성 요청 */
  public record CreateRequest(
      @NotBlank @Size(max = 80) String approvalCode,
      @NotBlank @Size(max = 200) String approvalName,
      String description,
      boolean useRequestDepartment,
      boolean useSupervisingDepartment,
      Long defaultSupervisingDepartmentId,
      boolean useGroupApproval,
      boolean usePersonalLineTemplate,
      boolean activeYn,
      Integer sortOrder,
      String remark
  ) {}

  /** 수정 요청 */
  public record UpdateRequest(
      @NotBlank @Size(max = 200) String approvalName,
      String description,
      boolean useRequestDepartment,
      boolean useSupervisingDepartment,
      Long defaultSupervisingDepartmentId,
      boolean useGroupApproval,
      boolean usePersonalLineTemplate,
      boolean activeYn,
      Integer sortOrder,
      String remark
  ) {}

  /** 권한 규칙 생성 요청 */
  public record CreateAuthorityRuleRequest(
      Long targetDepartmentId,
      String targetRoleKey,
      @NotBlank String stepType
  ) {}
}

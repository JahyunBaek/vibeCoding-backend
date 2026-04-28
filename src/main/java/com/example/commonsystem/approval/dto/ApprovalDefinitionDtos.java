package com.example.commonsystem.approval.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 결재 기능 코드 마스터 관련 DTO */
public class ApprovalDefinitionDtos {

  /** 목록 행 - MyBatis 매핑 호환을 위해 Lombok 클래스 사용 */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class DefinitionListRow {
    private long definitionId;
    private long tenantId;
    private String approvalCode;
    private String approvalName;
    private String description;
    private boolean useRequestDepartment;
    private boolean useSupervisingDepartment;
    private Long defaultSupervisingDepartmentId;
    private String defaultSupervisingDepartmentName;
    private boolean useGroupApproval;
    private boolean usePersonalLineTemplate;
    private boolean activeYn;
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
  }

  /** 상세 (+ 권한 규칙 목록) - MyBatis nested collection 매핑 위해 Lombok 클래스 사용 */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class DefinitionDetail {
    private long definitionId;
    private long tenantId;
    private String approvalCode;
    private String approvalName;
    private String description;
    private boolean useRequestDepartment;
    private boolean useSupervisingDepartment;
    private Long defaultSupervisingDepartmentId;
    private boolean useGroupApproval;
    private boolean usePersonalLineTemplate;
    private boolean activeYn;
    private int sortOrder;
    private String remark;
    private List<AuthorityRuleRow> authorityRules;
    /** 정책 필수 단계 — 사용자 결재선 뒤에 강제로 붙음 */
    private List<RequiredStepRow> requiredSteps;
  }

  /** 필수 단계 행 (조회용) */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class RequiredStepRow {
    private Long requiredStepId;
    private long definitionId;
    private int stepOrder;
    private String stepName;
    private String approvalType;
    private String targetDepartmentType;
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private String targetRoleKey;
    private Long targetUserId;
    private String targetUserName;
    private boolean groupApprovalYn;
  }

  /** 권한 규칙 행 - MyBatis 매핑 호환을 위해 Lombok 클래스 사용 */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class AuthorityRuleRow {
    private Long ruleId;
    private String approvalCode;
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private String targetRoleKey;
    private String stepType;
    private boolean activeYn;
  }

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

  /** 필수 단계 추가 요청 */
  public record CreateRequiredStepRequest(
      Integer stepOrder,                       // null이면 마지막에 추가
      @NotBlank @Size(max = 100) String stepName,
      String approvalType,                     // APPROVE 기본
      @NotBlank String targetDepartmentType,   // REQUEST | SUPERVISING | CUSTOM | USER
      Long targetDepartmentId,
      String targetRoleKey,
      Long targetUserId,
      Boolean groupApprovalYn
  ) {}
}

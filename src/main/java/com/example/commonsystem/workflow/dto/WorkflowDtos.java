package com.example.commonsystem.workflow.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 공통 워크플로우 DTO */
public class WorkflowDtos {

  // ── 상태 (글로벌 풀) ──
  public record StateRow(
      String stateCode,
      String stateName,
      String stateType,
      String color,
      String description,
      int sortOrder
  ) {}

  // ── 워크플로우 정의 ──
  @Getter @Setter @NoArgsConstructor
  public static class WorkflowListRow {
    private long workflowId;
    private Long tenantId;
    private String workflowCode;
    private String workflowName;
    private String description;
    private String entityType;
    private String initialStateCode;
    private boolean activeYn;
    private boolean isTemplate;
    private Long parentWorkflowId;
    private Integer templateVersion;
    private int transitionCount;
    private int instanceCount;
    private Instant createdAt;
    private Instant updatedAt;
  }

  @Getter @Setter @NoArgsConstructor
  public static class WorkflowDetail {
    private long workflowId;
    private Long tenantId;
    private String workflowCode;
    private String workflowName;
    private String description;
    private String entityType;
    private String initialStateCode;
    private boolean activeYn;
    private boolean isTemplate;
    private Long parentWorkflowId;
    private Integer templateVersion;
    private List<TransitionRow> transitions;
  }

  @Getter @Setter @NoArgsConstructor
  public static class TransitionRow {
    private long transitionId;
    private String actionCode;
    private String actionName;
    private String fromStateCode;
    private String toStateCode;
    private String buttonColor;
    private String buttonIcon;
    private boolean commentRequired;
    private boolean autoSkip;
    private int sortOrder;
    private boolean activeYn;
    private List<ConditionRow> conditions;
    private List<PostActionRow> postActions;
  }

  public record ConditionRow(
      long conditionId,
      String conditionType,
      String roleKey,
      Long deptId,
      Long userId
  ) {}

  public record PostActionRow(
      long postActionId,
      String actionType,
      String approvalCode,
      String notifyTarget,
      String notifyTemplate,
      String fieldPath,
      String fieldValue
  ) {}

  // ── 시스템 템플릿 작성 (SUPER_ADMIN) ──
  public record CreateWorkflowRequest(
      @NotBlank @Size(max = 50) String workflowCode,
      @NotBlank @Size(max = 200) String workflowName,
      @Size(max = 500) String description,
      @NotBlank @Size(max = 80) String entityType,
      @NotBlank String initialStateCode,
      @NotEmpty List<TransitionInput> transitions
  ) {}

  public record UpdateWorkflowRequest(
      @NotBlank @Size(max = 200) String workflowName,
      @Size(max = 500) String description,
      @NotBlank String initialStateCode,
      boolean activeYn,
      @NotEmpty List<TransitionInput> transitions
  ) {}

  public record TransitionInput(
      Long transitionId,                          // 수정 시 기존 ID 유지
      @NotBlank String actionCode,
      @NotBlank String actionName,
      @NotBlank String fromStateCode,
      @NotBlank String toStateCode,
      String buttonColor,
      String buttonIcon,
      boolean commentRequired,
      boolean autoSkip,
      int sortOrder,
      boolean activeYn,
      List<ConditionInput> conditions,
      List<PostActionInput> postActions
  ) {}

  public record ConditionInput(
      @NotBlank String conditionType,            // ROLE/DEPT/USER/IS_REQUESTER/IS_ASSIGNEE/ANY
      String roleKey,
      Long deptId,
      Long userId
  ) {}

  public record PostActionInput(
      @NotBlank String actionType,                // REQUIRE_APPROVAL/NOTIFY/ASSIGN/UPDATE_FIELD
      String approvalCode,
      String notifyTarget,
      String notifyTemplate,
      String fieldPath,
      String fieldValue
  ) {}

  // ── 인스턴스 ──
  @Getter @Setter @NoArgsConstructor
  public static class InstanceDetail {
    private long instanceId;
    private long tenantId;
    private long workflowId;
    private String workflowCode;
    private String workflowName;
    private String entityType;
    private long entityId;
    private String currentStateCode;
    private String currentStateName;
    private String currentStateColor;
    private String status;
    private Long requesterUserId;
    private String requesterName;
    private Long assigneeUserId;
    private String assigneeName;
    private Long approvalDocumentId;
    private List<HistoryRow> history;
    private List<AvailableAction> availableActions;
    private Instant createdAt;
    private Instant updatedAt;
  }

  @Getter @Setter @NoArgsConstructor
  public static class HistoryRow {
    private long historyId;
    private String actionCode;
    private String fromStateCode;
    private String toStateCode;
    private Long actorUserId;
    private String actorName;
    private String comment;
    private Instant createdAt;
  }

  /** 현재 사용자가 가능한 전이 — 프론트엔드 버튼 자동 렌더링용 */
  public record AvailableAction(
      long transitionId,
      String actionCode,
      String actionName,
      String toStateCode,
      String buttonColor,
      String buttonIcon,
      boolean commentRequired,
      boolean requiresApproval
  ) {}

  // ── 인스턴스 액션 요청 ──
  public record TransitionRequest(
      @NotBlank String actionCode,
      @Size(max = 1000) String comment,
      Long assigneeUserId    // ASSIGN 액션에서 담당자 지정 시 사용
  ) {}

  /** 다른 도메인 서비스가 인스턴스 시작 시 사용 */
  public record StartInstanceRequest(
      @NotBlank String workflowCode,
      @NotBlank String entityType,
      long entityId,
      Long requesterUserId,
      Long assigneeUserId
  ) {}
}

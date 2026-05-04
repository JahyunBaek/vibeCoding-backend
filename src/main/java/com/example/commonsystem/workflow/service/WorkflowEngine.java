package com.example.commonsystem.workflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.notification.service.NotificationService;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.workflow.dto.WorkflowDtos.AvailableAction;
import com.example.commonsystem.workflow.dto.WorkflowDtos.HistoryRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.InstanceDetail;
import com.example.commonsystem.workflow.dto.WorkflowDtos.PostActionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.StartInstanceRequest;
import com.example.commonsystem.workflow.dto.WorkflowDtos.TransitionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowDetail;
import com.example.commonsystem.workflow.mapper.WorkflowMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 다른 도메인이 주입해서 사용하는 워크플로우 진입점(Facade).
 *
 * 사용 예 (비즈니스 도메인 서비스에서):
 * <pre>
 * @Service
 * public class IncidentService {
 *   private final WorkflowEngine workflow;
 *
 *   public long create(...) {
 *     long incidentId = mapper.insert(...);
 *     workflow.startInstance(new StartInstanceRequest(
 *         "ITSM_INCIDENT", "Incident", incidentId, user.getUserId(), null));
 *     return incidentId;
 *   }
 *
 *   public void resolve(long incidentId, UserPrincipal user, String comment) {
 *     workflow.transition("Incident", incidentId, "RESOLVE", comment, null, user);
 *   }
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

  private final WorkflowMapper mapper;
  private final TransitionGuard guard;
  private final TenantContextHolder tenantCtx;
  private final NotificationService notificationService;

  // ── 인스턴스 시작 ──
  @Transactional
  public long startInstance(StartInstanceRequest req) {
    Long tenantId = tenantCtx.currentTenantId();
    if (tenantId == null) {
      throw new AppException(ErrorCode.VALIDATION,
          "SUPER_ADMIN 은 워크플로우 인스턴스를 시작할 수 없습니다.");
    }
    WorkflowDetail def = mapper.findDefinitionByCode(tenantId, req.workflowCode());
    if (def == null) {
      throw new AppException(ErrorCode.NOT_FOUND,
          "테넌트에 등록된 워크플로우가 없습니다: " + req.workflowCode());
    }
    long instanceId = mapper.insertInstance(tenantId, def.getWorkflowId(),
        req.entityType(), req.entityId(), def.getInitialStateCode(),
        req.requesterUserId(), req.assigneeUserId());
    mapper.insertHistory(instanceId, null, "START", null, def.getInitialStateCode(),
        req.requesterUserId(), null, null);
    return instanceId;
  }

  // ── 인스턴스 조회 ──
  public InstanceDetail detailByEntity(String entityType, long entityId, UserPrincipal user) {
    Long tenantId = tenantCtx.currentTenantId();
    Map<String, Object> row = mapper.findInstanceByEntity(tenantId, entityType, entityId);
    if (row == null) {
      throw new AppException(ErrorCode.NOT_FOUND, "워크플로우 인스턴스가 없습니다.");
    }
    return buildDetail(row, user);
  }

  // ── 상태 전이 (entity_type + entity_id 기준) ──
  @Transactional
  public void transition(String entityType, long entityId, String actionCode,
                         String comment, Long assigneeUserIdParam, UserPrincipal user) {
    Long tenantId = tenantCtx.currentTenantId();
    Map<String, Object> inst = mapper.findInstanceByEntity(tenantId, entityType, entityId);
    if (inst == null) {
      throw new AppException(ErrorCode.NOT_FOUND, "워크플로우 인스턴스가 없습니다.");
    }
    long instanceId = ((Number) inst.get("instanceId")).longValue();
    long workflowId = ((Number) inst.get("workflowId")).longValue();
    String currentState = (String) inst.get("currentStateCode");
    String status = (String) inst.get("status");
    Long requesterUserId = toLong(inst.get("requesterUserId"));
    Long assigneeUserId = toLong(inst.get("assigneeUserId"));

    if (!"ACTIVE".equals(status)) {
      throw new AppException(ErrorCode.CONFLICT, "현재 상태에서 전이할 수 없습니다: " + status);
    }

    TransitionRow tr = findTransitionByAction(workflowId, currentState, actionCode);
    if (tr == null) {
      throw new AppException(ErrorCode.VALIDATION,
          "유효하지 않은 액션입니다: " + actionCode + " (현재 상태: " + currentState + ")");
    }
    if (tr.isCommentRequired() && (comment == null || comment.isBlank())) {
      throw new AppException(ErrorCode.VALIDATION, "이 액션은 의견 입력이 필수입니다.");
    }
    if (!guard.canTransition(tr.getTransitionId(), user, requesterUserId, assigneeUserId)) {
      throw new AppException(ErrorCode.FORBIDDEN, "이 액션을 수행할 권한이 없습니다.");
    }

    List<PostActionRow> postActions = mapper.findPostActionsByTransition(tr.getTransitionId());

    // REQUIRE_APPROVAL: 결재 정책 시작 후 결재 완료 시 자동 전이 (Phase 1: 결재 발행 hook 표시만, 실제 결재 발행은 도메인 책임)
    boolean requiresApproval = postActions.stream()
        .anyMatch(p -> "REQUIRE_APPROVAL".equals(p.actionType()));
    if (requiresApproval) {
      // 인스턴스를 PENDING_APPROVAL 로 전환하고 pending_transition_id 보관
      mapper.updateInstanceApproval(instanceId, null, tr.getTransitionId(), "PENDING_APPROVAL");
      mapper.insertHistory(instanceId, tr.getTransitionId(), actionCode,
          currentState, currentState, user.getUserId(), comment, null);
      log.info("Workflow transition pending approval: instance={}, action={}, approvalCode={}",
          instanceId, actionCode,
          postActions.stream()
              .filter(p -> "REQUIRE_APPROVAL".equals(p.actionType()))
              .findFirst().map(PostActionRow::approvalCode).orElse(null));
      return;
    }

    // 즉시 전이
    applyTransition(instanceId, tr, currentState, user, comment, assigneeUserIdParam,
        requesterUserId, assigneeUserId, postActions);
  }

  /**
   * 결재 시스템에서 결재 완료 시 호출.
   * pending_transition_id 를 적용해 다음 상태로 진행.
   */
  @Transactional
  public void onApprovalCompleted(long instanceId, boolean approved, UserPrincipal user, String comment) {
    Map<String, Object> inst = mapper.findInstanceById(instanceId, null);
    if (inst == null) return;
    Long pendingTrId = toLong(inst.get("pendingTransitionId"));
    if (pendingTrId == null) return;

    TransitionRow tr = mapper.findTransitionById(pendingTrId);
    String currentState = (String) inst.get("currentStateCode");
    Long requesterUserId = toLong(inst.get("requesterUserId"));
    Long assigneeUserId = toLong(inst.get("assigneeUserId"));

    if (approved) {
      List<PostActionRow> postActions = mapper.findPostActionsByTransition(pendingTrId);
      applyTransition(instanceId, tr, currentState, user, comment, null,
          requesterUserId, assigneeUserId, postActions);
    } else {
      // 반려 — 결재 취소, 인스턴스 ACTIVE 로 복귀
      mapper.updateInstanceApproval(instanceId, null, null, "ACTIVE");
      mapper.insertHistory(instanceId, pendingTrId, "APPROVAL_REJECTED",
          currentState, currentState, user != null ? user.getUserId() : null, comment, null);
    }
  }

  private void applyTransition(long instanceId, TransitionRow tr, String currentState,
                                UserPrincipal user, String comment, Long assigneeUserIdParam,
                                Long requesterUserId, Long assigneeUserId,
                                List<PostActionRow> postActions) {
    String toState = tr.getToStateCode();

    // 담당자 변경 (ASSIGN 액션 - assigneeUserIdParam 또는 post-action ASSIGN)
    Long newAssignee = assigneeUserIdParam != null ? assigneeUserIdParam : assigneeUserId;

    mapper.updateInstanceState(instanceId, toState, "ACTIVE");
    if (assigneeUserIdParam != null) {
      mapper.updateInstanceAssignee(instanceId, assigneeUserIdParam);
    }
    mapper.insertHistory(instanceId, tr.getTransitionId(), tr.getActionCode(),
        currentState, toState, user != null ? user.getUserId() : null, comment, null);

    // post-actions 처리
    Long tenantId = tenantCtx.currentTenantId();
    for (PostActionRow p : postActions) {
      switch (p.actionType()) {
        case "NOTIFY" -> sendNotification(p, instanceId, tenantId, requesterUserId, newAssignee, tr);
        case "ASSIGN" -> {
          // field_value 에 user_id 가 있으면 그 사용자, 없으면 무시
          if (p.fieldValue() != null) {
            try { mapper.updateInstanceAssignee(instanceId, Long.parseLong(p.fieldValue())); }
            catch (NumberFormatException e) { log.warn("Invalid ASSIGN value: {}", p.fieldValue()); }
          }
        }
        // UPDATE_FIELD / 기타는 비즈니스 로직 후크가 필요 — 도메인에서 처리
        default -> {}
      }
    }
  }

  private void sendNotification(PostActionRow p, long instanceId, Long tenantId,
                                 Long requesterUserId, Long assigneeUserId, TransitionRow tr) {
    String target = p.notifyTarget();
    if (target == null) return;
    Long userId = null;
    if ("REQUESTER".equals(target)) userId = requesterUserId;
    else if ("ASSIGNEE".equals(target)) userId = assigneeUserId;
    else if (target.startsWith("USER:")) {
      try { userId = Long.parseLong(target.substring(5)); }
      catch (NumberFormatException e) { return; }
    }
    if (userId == null) return;
    String title = "[Workflow] " + tr.getActionName();
    String message = "워크플로우 단계: " + tr.getFromStateCode() + " → " + tr.getToStateCode();
    notificationService.create(tenantId, userId, "WORKFLOW", title, message,
        "/workflow/instances/" + instanceId);
  }

  private TransitionRow findTransitionByAction(long workflowId, String fromState, String actionCode) {
    return mapper.findTransitionsByWorkflow(workflowId).stream()
        .filter(tr -> tr.isActiveYn()
            && fromState.equals(tr.getFromStateCode())
            && actionCode.equals(tr.getActionCode()))
        .findFirst().orElse(null);
  }

  private InstanceDetail buildDetail(Map<String, Object> row, UserPrincipal user) {
    InstanceDetail d = new InstanceDetail();
    long instanceId = ((Number) row.get("instanceId")).longValue();
    long workflowId = ((Number) row.get("workflowId")).longValue();
    d.setInstanceId(instanceId);
    d.setTenantId(((Number) row.get("tenantId")).longValue());
    d.setWorkflowId(workflowId);
    d.setWorkflowCode((String) row.get("workflowCode"));
    d.setWorkflowName((String) row.get("workflowName"));
    d.setEntityType((String) row.get("entityType"));
    d.setEntityId(((Number) row.get("entityId")).longValue());
    d.setCurrentStateCode((String) row.get("currentStateCode"));
    d.setCurrentStateName((String) row.get("currentStateName"));
    d.setCurrentStateColor((String) row.get("currentStateColor"));
    d.setStatus((String) row.get("status"));
    d.setRequesterUserId(toLong(row.get("requesterUserId")));
    d.setRequesterName((String) row.get("requesterName"));
    d.setAssigneeUserId(toLong(row.get("assigneeUserId")));
    d.setAssigneeName((String) row.get("assigneeName"));
    d.setApprovalDocumentId(toLong(row.get("approvalDocumentId")));
    d.setCreatedAt(toInstant(row.get("createdAt")));
    d.setUpdatedAt(toInstant(row.get("updatedAt")));

    // 이력
    List<HistoryRow> history = mapper.findHistory(instanceId).stream().map(h -> {
      HistoryRow hr = new HistoryRow();
      hr.setHistoryId(((Number) h.get("historyId")).longValue());
      hr.setActionCode((String) h.get("actionCode"));
      hr.setFromStateCode((String) h.get("fromStateCode"));
      hr.setToStateCode((String) h.get("toStateCode"));
      hr.setActorUserId(toLong(h.get("actorUserId")));
      hr.setActorName((String) h.get("actorName"));
      hr.setComment((String) h.get("comment"));
      hr.setCreatedAt(toInstant(h.get("createdAt")));
      return hr;
    }).collect(Collectors.toList());
    d.setHistory(history);

    // 사용 가능 액션 (현재 상태 + 사용자 권한 기반)
    List<AvailableAction> actions = new ArrayList<>();
    if ("ACTIVE".equals(d.getStatus())) {
      List<TransitionRow> trs = mapper.findTransitionsByWorkflow(workflowId);
      for (TransitionRow tr : trs) {
        if (!tr.isActiveYn() || !d.getCurrentStateCode().equals(tr.getFromStateCode())) continue;
        if (!guard.canTransition(tr.getTransitionId(), user,
            d.getRequesterUserId(), d.getAssigneeUserId())) continue;
        boolean requiresApproval = mapper.findPostActionsByTransition(tr.getTransitionId()).stream()
            .anyMatch(p -> "REQUIRE_APPROVAL".equals(p.actionType()));
        actions.add(new AvailableAction(tr.getTransitionId(), tr.getActionCode(), tr.getActionName(),
            tr.getToStateCode(), tr.getButtonColor(), tr.getButtonIcon(),
            tr.isCommentRequired(), requiresApproval));
      }
    }
    d.setAvailableActions(actions);
    return d;
  }

  private static Long toLong(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return n.longValue();
    return null;
  }

  private static java.time.Instant toInstant(Object o) {
    if (o == null) return null;
    if (o instanceof java.time.Instant i) return i;
    if (o instanceof java.sql.Timestamp ts) return ts.toInstant();
    return null;
  }
}

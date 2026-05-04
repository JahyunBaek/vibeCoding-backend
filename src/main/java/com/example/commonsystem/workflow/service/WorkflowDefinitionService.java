package com.example.commonsystem.workflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.workflow.dto.WorkflowDtos.ConditionInput;
import com.example.commonsystem.workflow.dto.WorkflowDtos.ConditionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.CreateWorkflowRequest;
import com.example.commonsystem.workflow.dto.WorkflowDtos.PostActionInput;
import com.example.commonsystem.workflow.dto.WorkflowDtos.PostActionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.StateRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.TransitionInput;
import com.example.commonsystem.workflow.dto.WorkflowDtos.TransitionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.UpdateWorkflowRequest;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowDetail;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowListRow;
import com.example.commonsystem.workflow.mapper.WorkflowMapper;

import lombok.RequiredArgsConstructor;

/**
 * 워크플로우 정의 관리.
 *
 * 권한 모델:
 * - 시스템 템플릿 (tenant_id IS NULL): SUPER_ADMIN 만 작성/수정 가능
 * - 테넌트 워크플로우 (tenant_id = N): ADMIN 이 자기 테넌트만 관리
 * - 컨트롤러 레이어에서 @PreAuthorize 로 강제, 서비스는 호출자 권한 위임 가정
 */
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionService {

  private final WorkflowMapper mapper;
  private final TenantContextHolder tenantCtx;

  // ── 글로벌 상태 풀 ──
  public List<StateRow> allStates() {
    return mapper.findAllStates();
  }

  // ── 시스템 템플릿 (SUPER_ADMIN) ──
  public List<WorkflowListRow> listTemplates() {
    return mapper.findTemplates();
  }

  @Transactional
  public long createTemplate(CreateWorkflowRequest req, Long createdBy) {
    if (mapper.findDefinitionByCode(null, req.workflowCode()) != null) {
      throw new AppException(ErrorCode.CONFLICT, "이미 존재하는 워크플로우 코드: " + req.workflowCode());
    }
    long workflowId = insertWithTransitions(null, req.workflowCode(), req.workflowName(),
        req.description(), req.entityType(), req.initialStateCode(),
        true, null, 1, true, createdBy, req.transitions());
    return workflowId;
  }

  @Transactional
  public void updateTemplate(long workflowId, UpdateWorkflowRequest req) {
    WorkflowDetail existing = mapper.findDefinitionById(workflowId, null);
    if (existing == null || !existing.isTemplate()) {
      throw new AppException(ErrorCode.NOT_FOUND, "시스템 템플릿을 찾을 수 없습니다.");
    }
    mapper.updateDefinition(workflowId, null,
        req.workflowName(), req.description(), req.initialStateCode(), req.activeYn());
    replaceTransitions(workflowId, req.transitions());
  }

  // ── 테넌트 워크플로우 (ADMIN) ──
  public List<WorkflowListRow> listForTenant(String entityType, boolean activeOnly) {
    return mapper.findByTenant(tenantCtx.currentTenantId(), entityType, activeOnly);
  }

  public WorkflowDetail detail(long workflowId) {
    Long tenantId = tenantCtx.currentTenantId();
    WorkflowDetail d = mapper.findDefinitionById(workflowId, tenantId);
    if (d == null) throw new AppException(ErrorCode.NOT_FOUND, "워크플로우를 찾을 수 없습니다.");
    d.setTransitions(loadFullTransitions(workflowId));
    return d;
  }

  /**
   * 시스템 템플릿을 테넌트로 복사 (Copy-on-Customize).
   * 같은 workflow_code 가 이미 테넌트에 존재하면 충돌.
   */
  @Transactional
  public long copyTemplateToTenant(long templateWorkflowId, Long createdBy) {
    Long tenantId = tenantCtx.currentTenantId();
    if (tenantId == null) {
      throw new AppException(ErrorCode.VALIDATION, "SUPER_ADMIN 은 테넌트 워크플로우를 보유할 수 없습니다.");
    }
    WorkflowDetail tpl = mapper.findDefinitionById(templateWorkflowId, null);
    if (tpl == null || !tpl.isTemplate()) {
      throw new AppException(ErrorCode.NOT_FOUND, "시스템 템플릿을 찾을 수 없습니다.");
    }
    if (mapper.findDefinitionByCode(tenantId, tpl.getWorkflowCode()) != null) {
      throw new AppException(ErrorCode.CONFLICT,
          "이미 같은 코드의 워크플로우가 존재합니다: " + tpl.getWorkflowCode());
    }

    // 정의 복사
    long newWorkflowId = mapper.insertDefinition(tenantId, tpl.getWorkflowCode(),
        tpl.getWorkflowName(), tpl.getDescription(), tpl.getEntityType(),
        tpl.getInitialStateCode(), false, templateWorkflowId, tpl.getTemplateVersion(),
        true, createdBy);

    // 전이/조건/post-action 복사
    List<TransitionRow> trans = loadFullTransitions(templateWorkflowId);
    for (TransitionRow tr : trans) {
      long newTrId = mapper.insertTransition(newWorkflowId, tr.getActionCode(), tr.getActionName(),
          tr.getFromStateCode(), tr.getToStateCode(), tr.getButtonColor(), tr.getButtonIcon(),
          tr.isCommentRequired(), tr.isAutoSkip(), tr.getSortOrder(), tr.isActiveYn());
      int idx = 0;
      if (tr.getConditions() != null) {
        for (ConditionRow c : tr.getConditions()) {
          mapper.insertCondition(newTrId,
              new ConditionInput(c.conditionType(), c.roleKey(), c.deptId(), c.userId()), idx++);
        }
      }
      idx = 0;
      if (tr.getPostActions() != null) {
        for (PostActionRow p : tr.getPostActions()) {
          mapper.insertPostAction(newTrId,
              new PostActionInput(p.actionType(), p.approvalCode(), p.notifyTarget(),
                  p.notifyTemplate(), p.fieldPath(), p.fieldValue()), idx++);
        }
      }
    }
    return newWorkflowId;
  }

  @Transactional
  public void updateTenant(long workflowId, UpdateWorkflowRequest req) {
    Long tenantId = tenantCtx.currentTenantId();
    if (tenantId == null) {
      throw new AppException(ErrorCode.VALIDATION, "SUPER_ADMIN 은 이 API 를 사용할 수 없습니다.");
    }
    WorkflowDetail existing = mapper.findDefinitionById(workflowId, tenantId);
    if (existing == null || existing.isTemplate()) {
      throw new AppException(ErrorCode.NOT_FOUND, "테넌트 워크플로우를 찾을 수 없습니다.");
    }
    mapper.updateDefinition(workflowId, tenantId,
        req.workflowName(), req.description(), req.initialStateCode(), req.activeYn());
    replaceTransitions(workflowId, req.transitions());
  }

  @Transactional
  public void delete(long workflowId, boolean isSuperAdmin) {
    Long tenantId = isSuperAdmin ? null : tenantCtx.currentTenantId();
    mapper.deleteDefinition(workflowId, tenantId);
  }

  // ── 헬퍼 ──
  private long insertWithTransitions(Long tenantId, String workflowCode, String workflowName,
                                      String description, String entityType, String initialStateCode,
                                      boolean isTemplate, Long parentWorkflowId, Integer templateVersion,
                                      boolean activeYn, Long createdBy,
                                      List<TransitionInput> transitions) {
    long workflowId = mapper.insertDefinition(tenantId, workflowCode, workflowName, description,
        entityType, initialStateCode, isTemplate, parentWorkflowId, templateVersion,
        activeYn, createdBy);
    insertTransitions(workflowId, transitions);
    return workflowId;
  }

  private void replaceTransitions(long workflowId, List<TransitionInput> transitions) {
    mapper.deleteTransitionsByWorkflow(workflowId); // CASCADE 로 conditions / post_actions 삭제됨
    insertTransitions(workflowId, transitions);
  }

  private void insertTransitions(long workflowId, List<TransitionInput> transitions) {
    if (transitions == null) return;
    for (TransitionInput t : transitions) {
      long transitionId = mapper.insertTransition(workflowId, t.actionCode(), t.actionName(),
          t.fromStateCode(), t.toStateCode(), t.buttonColor(), t.buttonIcon(),
          t.commentRequired(), t.autoSkip(), t.sortOrder(), t.activeYn());
      int idx = 0;
      if (t.conditions() != null) {
        for (ConditionInput c : t.conditions()) {
          mapper.insertCondition(transitionId, c, idx++);
        }
      }
      idx = 0;
      if (t.postActions() != null) {
        for (PostActionInput p : t.postActions()) {
          mapper.insertPostAction(transitionId, p, idx++);
        }
      }
    }
  }

  private List<TransitionRow> loadFullTransitions(long workflowId) {
    List<TransitionRow> rows = mapper.findTransitionsByWorkflow(workflowId);
    for (TransitionRow tr : rows) {
      tr.setConditions(mapper.findConditionsByTransition(tr.getTransitionId()));
      tr.setPostActions(mapper.findPostActionsByTransition(tr.getTransitionId()));
    }
    return rows;
  }
}

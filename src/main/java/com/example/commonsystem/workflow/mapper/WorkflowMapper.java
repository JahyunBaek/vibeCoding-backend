package com.example.commonsystem.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.workflow.dto.WorkflowDtos.ConditionInput;
import com.example.commonsystem.workflow.dto.WorkflowDtos.ConditionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.PostActionInput;
import com.example.commonsystem.workflow.dto.WorkflowDtos.PostActionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.StateRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.TransitionRow;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowDetail;
import com.example.commonsystem.workflow.dto.WorkflowDtos.WorkflowListRow;

@Mapper
public interface WorkflowMapper {

  // ── 상태 ──
  List<StateRow> findAllStates();

  // ── 워크플로우 정의 ──
  /** tenant_id IS NULL 인 시스템 템플릿만 조회 (isTemplate=true 필터) */
  List<WorkflowListRow> findTemplates();

  /** 테넌트의 워크플로우 (tenant_id 일치) */
  List<WorkflowListRow> findByTenant(@Param("tenantId") Long tenantId,
                                     @Param("entityType") String entityType,
                                     @Param("activeOnly") boolean activeOnly);

  WorkflowDetail findDefinitionById(@Param("workflowId") long workflowId,
                                    @Param("tenantId") Long tenantId);

  WorkflowDetail findDefinitionByCode(@Param("tenantId") Long tenantId,
                                      @Param("workflowCode") String workflowCode);

  long insertDefinition(@Param("tenantId") Long tenantId,
                        @Param("workflowCode") String workflowCode,
                        @Param("workflowName") String workflowName,
                        @Param("description") String description,
                        @Param("entityType") String entityType,
                        @Param("initialStateCode") String initialStateCode,
                        @Param("isTemplate") boolean isTemplate,
                        @Param("parentWorkflowId") Long parentWorkflowId,
                        @Param("templateVersion") Integer templateVersion,
                        @Param("activeYn") boolean activeYn,
                        @Param("createdBy") Long createdBy);

  void updateDefinition(@Param("workflowId") long workflowId,
                        @Param("tenantId") Long tenantId,
                        @Param("workflowName") String workflowName,
                        @Param("description") String description,
                        @Param("initialStateCode") String initialStateCode,
                        @Param("activeYn") boolean activeYn);

  void deleteDefinition(@Param("workflowId") long workflowId,
                        @Param("tenantId") Long tenantId);

  // ── 전이 ──
  List<TransitionRow> findTransitionsByWorkflow(@Param("workflowId") long workflowId);

  TransitionRow findTransitionById(@Param("transitionId") long transitionId);

  long insertTransition(@Param("workflowId") long workflowId,
                        @Param("actionCode") String actionCode,
                        @Param("actionName") String actionName,
                        @Param("fromStateCode") String fromStateCode,
                        @Param("toStateCode") String toStateCode,
                        @Param("buttonColor") String buttonColor,
                        @Param("buttonIcon") String buttonIcon,
                        @Param("commentRequired") boolean commentRequired,
                        @Param("autoSkip") boolean autoSkip,
                        @Param("sortOrder") int sortOrder,
                        @Param("activeYn") boolean activeYn);

  void deleteTransitionsByWorkflow(@Param("workflowId") long workflowId);

  // ── 조건 / Post-Action ──
  List<ConditionRow> findConditionsByTransition(@Param("transitionId") long transitionId);
  List<PostActionRow> findPostActionsByTransition(@Param("transitionId") long transitionId);

  void insertCondition(@Param("transitionId") long transitionId,
                       @Param("c") ConditionInput c,
                       @Param("sortOrder") int sortOrder);

  void insertPostAction(@Param("transitionId") long transitionId,
                        @Param("p") PostActionInput p,
                        @Param("sortOrder") int sortOrder);

  // ── 인스턴스 ──
  long insertInstance(@Param("tenantId") long tenantId,
                      @Param("workflowId") long workflowId,
                      @Param("entityType") String entityType,
                      @Param("entityId") long entityId,
                      @Param("currentStateCode") String currentStateCode,
                      @Param("requesterUserId") Long requesterUserId,
                      @Param("assigneeUserId") Long assigneeUserId);

  java.util.Map<String, Object> findInstanceByEntity(@Param("tenantId") Long tenantId,
                                                      @Param("entityType") String entityType,
                                                      @Param("entityId") long entityId);

  java.util.Map<String, Object> findInstanceById(@Param("instanceId") long instanceId,
                                                  @Param("tenantId") Long tenantId);

  void updateInstanceState(@Param("instanceId") long instanceId,
                           @Param("currentStateCode") String currentStateCode,
                           @Param("status") String status);

  void updateInstanceAssignee(@Param("instanceId") long instanceId,
                              @Param("assigneeUserId") Long assigneeUserId);

  void updateInstanceApproval(@Param("instanceId") long instanceId,
                              @Param("approvalDocumentId") Long approvalDocumentId,
                              @Param("pendingTransitionId") Long pendingTransitionId,
                              @Param("status") String status);

  void insertHistory(@Param("instanceId") long instanceId,
                     @Param("transitionId") Long transitionId,
                     @Param("actionCode") String actionCode,
                     @Param("fromStateCode") String fromStateCode,
                     @Param("toStateCode") String toStateCode,
                     @Param("actorUserId") Long actorUserId,
                     @Param("comment") String comment,
                     @Param("meta") String metaJson);

  List<java.util.Map<String, Object>> findHistory(@Param("instanceId") long instanceId);
}

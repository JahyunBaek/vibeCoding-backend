package com.example.commonsystem.workflow.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.workflow.dto.WorkflowDtos.ConditionRow;
import com.example.commonsystem.workflow.mapper.WorkflowMapper;

import lombok.RequiredArgsConstructor;

/**
 * 전이 권한 검사. workflow_transition_conditions 의 조건들을 OR 로 평가한다.
 *
 * 조건 타입:
 *   ANY            — 인증된 모든 사용자
 *   ROLE           — 사용자 role_key 가 일치
 *   DEPT           — 사용자 org_id 가 일치
 *   USER           — 사용자 user_id 가 일치
 *   IS_REQUESTER   — 인스턴스의 요청자 본인
 *   IS_ASSIGNEE    — 인스턴스의 담당자 본인
 *
 * 조건이 하나도 없으면 "권한 미정" 으로 간주하여 허용 (정의 단계에서 보수적 가정).
 */
@Component
@RequiredArgsConstructor
public class TransitionGuard {

  private final WorkflowMapper mapper;

  public boolean canTransition(long transitionId, UserPrincipal user,
                                Long requesterUserId, Long assigneeUserId) {
    List<ConditionRow> conditions = mapper.findConditionsByTransition(transitionId);
    if (conditions == null || conditions.isEmpty()) {
      return true; // 조건 미정 — 허용
    }
    for (ConditionRow c : conditions) {
      if (matches(c, user, requesterUserId, assigneeUserId)) return true;
    }
    return false;
  }

  private boolean matches(ConditionRow c, UserPrincipal user,
                          Long requesterUserId, Long assigneeUserId) {
    if (user == null) return false;
    return switch (c.conditionType()) {
      case "ANY" -> true;
      case "ROLE" -> c.roleKey() != null && c.roleKey().equals(user.getRoleKey());
      case "DEPT" -> c.deptId() != null && c.deptId().equals(user.getOrgId());
      case "USER" -> c.userId() != null && c.userId().equals(user.getUserId());
      case "IS_REQUESTER" -> requesterUserId != null && requesterUserId.equals(user.getUserId());
      case "IS_ASSIGNEE" -> assigneeUserId != null && assigneeUserId.equals(user.getUserId());
      default -> false;
    };
  }
}

package com.example.commonsystem.workflow.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.workflow.dto.WorkflowDtos.InstanceDetail;
import com.example.commonsystem.workflow.dto.WorkflowDtos.TransitionRequest;
import com.example.commonsystem.workflow.service.WorkflowEngine;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 워크플로우 인스턴스 — 실제 비즈니스 엔티티의 상태 전이.
 * 일반 사용자(USER) 누구나 호출 가능 (전이 권한은 conditions 로 검사).
 *
 * URL 패턴:
 *   GET    /api/workflow/{entityType}/{entityId}                    — 현재 상태/이력/사용 가능 액션
 *   POST   /api/workflow/{entityType}/{entityId}/transition         — 상태 전이
 */
@Tag(name = "워크플로우 인스턴스", description = "비즈니스 엔티티의 워크플로우 상태 전이")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/workflow")
public class WorkflowInstanceController {

  private final WorkflowEngine engine;

  @Operation(summary = "엔티티의 워크플로우 인스턴스 조회")
  @GetMapping("/{entityType}/{entityId}")
  public ApiResponse<InstanceDetail> detail(@PathVariable String entityType,
                                             @PathVariable long entityId,
                                             @AuthenticationPrincipal UserPrincipal user) {
    return ApiResponse.ok(engine.detailByEntity(entityType, entityId, user));
  }

  @Operation(summary = "상태 전이 실행")
  @PostMapping("/{entityType}/{entityId}/transition")
  public ApiResponse<Void> transition(@PathVariable String entityType,
                                       @PathVariable long entityId,
                                       @Valid @RequestBody TransitionRequest req,
                                       @AuthenticationPrincipal UserPrincipal user) {
    engine.transition(entityType, entityId, req.actionCode(), req.comment(), req.assigneeUserId(), user);
    return ApiResponse.ok();
  }
}

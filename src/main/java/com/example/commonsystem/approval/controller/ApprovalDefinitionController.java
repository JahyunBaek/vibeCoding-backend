package com.example.commonsystem.approval.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionListRow;
import com.example.commonsystem.approval.service.ApprovalDefinitionService;
import com.example.commonsystem.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 일반 사용자용 결재 정책 조회.
 * 결재선 양식 작성, 결재 요청 시 활성 정책 목록을 가져오기 위해 사용.
 * Admin 전용 마스터 관리는 AdminApprovalDefinitionController 참조.
 */
@Tag(name = "결재 정책 조회 (사용자)", description = "활성 결재 정책 목록 조회")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/approval/definitions")
public class ApprovalDefinitionController {

  private final ApprovalDefinitionService service;

  @Operation(summary = "활성 결재 정책 목록")
  @GetMapping
  public ApiResponse<List<DefinitionListRow>> list(@RequestParam(required = false) String keyword) {
    // 일반 사용자는 자기 테넌트의 활성 정책만 조회 (tenantId override 없음)
    return ApiResponse.ok(service.list(true, keyword, null));
  }
}

package com.example.commonsystem.org.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.org.dto.OrgNode;
import com.example.commonsystem.org.service.OrgService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 일반 사용자가 조직 트리를 조회할 수 있는 가벼운 디렉토리 API.
 * AdminOrgController와 달리 ADMIN 권한 없이 호출 가능.
 * 결재선 작성, 멘션 등의 화면에서 사용.
 */
@Tag(name = "조직 디렉토리", description = "사용자용 조직 트리 조회")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orgs")
public class OrgDirectoryController {

  private final OrgService orgService;

  @Operation(summary = "조직 트리 조회 (자기 테넌트만)")
  @GetMapping("/tree")
  public ApiResponse<List<OrgNode>> tree() {
    return ApiResponse.ok(orgService.tree(null));
  }
}

package com.example.commonsystem.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.user.dto.UserListRow;
import com.example.commonsystem.user.mapper.UserMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 일반 사용자가 결재선/멘션 등에서 사용자 검색에 사용하는 가벼운 디렉토리 API.
 * AdminUserController와 달리 ADMIN 권한 없이 호출 가능.
 */
@Tag(name = "사용자 디렉토리", description = "결재선/멘션 등을 위한 사용자 조회")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UsersDirectoryController {

  private final UserMapper userMapper;
  private final TenantContextHolder tenantCtx;

  @Operation(summary = "사용자 검색 (이름/아이디)")
  @GetMapping("/search")
  public ApiResponse<List<UserListRow>> search(
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "20") int limit) {
    Long tenantId = tenantCtx.currentTenantId();
    int s = Math.min(Math.max(limit, 1), 100);
    List<UserListRow> users = userMapper.findPage(tenantId, orgId, s, 0);
    return ApiResponse.ok(users);
  }
}

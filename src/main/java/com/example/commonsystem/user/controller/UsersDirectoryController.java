package com.example.commonsystem.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.user.dto.UserDirectoryRow;
import com.example.commonsystem.user.mapper.UserMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 일반 사용자가 결재선/멘션 등에서 사용자 검색에 사용하는 디렉토리 API.
 * AdminUserController와 달리 ADMIN 권한 없이 호출 가능.
 *
 * 키워드 LIKE 검색 + 페이지네이션 (무한 스크롤 호환).
 */
@Tag(name = "사용자 디렉토리", description = "결재선/멘션 등을 위한 사용자 검색")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UsersDirectoryController {

  private final UserMapper userMapper;
  private final TenantContextHolder tenantCtx;

  @Operation(summary = "사용자 검색 (이름/아이디 + 부서 + 페이징)")
  @GetMapping("/search")
  public ApiResponse<PageResponse<UserDirectoryRow>> search(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "30") int size) {
    Long tenantId = tenantCtx.currentTenantId();
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

    long total = userMapper.searchCount(tenantId, orgId, kw);
    List<UserDirectoryRow> items = userMapper.searchPage(tenantId, orgId, kw, s, offset);
    return ApiResponse.ok(new PageResponse<>(items, p, s, total));
  }
}

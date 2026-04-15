package com.example.commonsystem.code.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.code.domain.CodeGroup;
import com.example.commonsystem.code.domain.CodeItem;
import com.example.commonsystem.code.dto.CodeCreateCommand;
import com.example.commonsystem.code.dto.CodeGroupCreateCommand;
import com.example.commonsystem.code.dto.CodeGroupUpdateCommand;
import com.example.commonsystem.code.dto.CodeUpdateCommand;
import com.example.commonsystem.code.service.CodeService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 공통코드", description = "공통코드 관리")
@RestController
@RequestMapping("/api/admin/codes")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminCodeController {

  private final CodeService codeService;
  private final TenantContextHolder tenantCtx;

  public AdminCodeController(CodeService codeService, TenantContextHolder tenantCtx) {
    this.codeService = codeService;
    this.tenantCtx = tenantCtx;
  }

  @Operation(summary = "코드그룹 목록 조회")
  @GetMapping("/groups")
  public ApiResponse<PageResponse<CodeGroup>> groups(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(codeService.groupPage(page, size));
  }

  @Operation(summary = "전체 코드그룹 조회")
  @GetMapping("/groups/all")
  public ApiResponse<List<CodeGroup>> allGroups() {
    return ApiResponse.ok(codeService.allGroups());
  }

  @Operation(summary = "코드그룹 생성")
  @PostMapping("/groups")
  public ApiResponse<Void> createGroup(@Valid @RequestBody CodeGroupCreateCommand cmd) {
    codeService.createGroup(new CodeGroupCreateCommand(cmd.groupKey(), cmd.groupName(), cmd.useYn(), tenantCtx.currentTenantId()));
    return ApiResponse.ok();
  }

  @Operation(summary = "코드그룹 수정")
  @PutMapping("/groups/{groupKey}")
  public ApiResponse<Void> updateGroup(@PathVariable String groupKey, @Valid @RequestBody CodeGroupUpdateCommand cmd) {
    codeService.updateGroup(new CodeGroupUpdateCommand(groupKey, cmd.groupName(), cmd.useYn(), tenantCtx.currentTenantId()));
    return ApiResponse.ok();
  }

  @Operation(summary = "코드그룹 삭제")
  @DeleteMapping("/groups/{groupKey}")
  public ApiResponse<Void> deleteGroup(@PathVariable String groupKey) {
    codeService.deleteGroup(groupKey);
    return ApiResponse.ok();
  }

  @Operation(summary = "코드항목 목록 조회")
  @GetMapping("/groups/{groupKey}/items")
  public ApiResponse<List<CodeItem>> items(@PathVariable String groupKey) {
    return ApiResponse.ok(codeService.listCodes(groupKey));
  }

  @Operation(summary = "코드항목 생성")
  @PostMapping("/groups/{groupKey}/items")
  public ApiResponse<Void> createItem(@PathVariable String groupKey, @Valid @RequestBody CodeCreateCommand cmd) {
    codeService.createCode(new CodeCreateCommand(groupKey, cmd.code(), cmd.name(), cmd.value(), cmd.sortOrder(), cmd.useYn(), tenantCtx.currentTenantId()));
    return ApiResponse.ok();
  }

  @Operation(summary = "코드항목 수정")
  @PutMapping("/groups/{groupKey}/items/{code}")
  public ApiResponse<Void> updateItem(@PathVariable String groupKey, @PathVariable String code, @Valid @RequestBody CodeUpdateCommand cmd) {
    codeService.updateCode(new CodeUpdateCommand(groupKey, code, cmd.name(), cmd.value(), cmd.sortOrder(), cmd.useYn(), tenantCtx.currentTenantId()));
    return ApiResponse.ok();
  }

  @Operation(summary = "코드항목 삭제")
  @DeleteMapping("/groups/{groupKey}/items/{code}")
  public ApiResponse<Void> deleteItem(@PathVariable String groupKey, @PathVariable String code) {
    codeService.deleteCode(groupKey, code);
    return ApiResponse.ok();
  }
}

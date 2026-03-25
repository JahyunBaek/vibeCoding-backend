package com.example.commonsystem.org.controller;

import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.org.domain.Org;
import com.example.commonsystem.org.dto.OrgCreateCommand;
import com.example.commonsystem.org.dto.OrgNode;
import com.example.commonsystem.org.dto.OrgUpdateCommand;
import com.example.commonsystem.org.service.OrgService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 조직", description = "조직 관리")
@RestController
@RequestMapping("/api/admin/orgs")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminOrgController {

  private final OrgService orgService;

  public AdminOrgController(OrgService orgService) {
    this.orgService = orgService;
  }

  @Operation(summary = "조직 트리 조회")
  @GetMapping("/tree")
  public ApiResponse<List<OrgNode>> tree(@RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(orgService.tree(tenantId));
  }

  @Operation(summary = "조직 목록 조회")
  @GetMapping
  public ApiResponse<PageResponse<Org>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) Long tenantId) {
    return ApiResponse.ok(orgService.page(page, size, tenantId));
  }

  public record CreateOrgRequest(Long parentId, @NotBlank @Size(max = 100) String name, int sortOrder, boolean useYn, Long tenantId) {}

  @Operation(summary = "조직 생성")
  @PostMapping
  public ApiResponse<Void> create(@Valid @RequestBody CreateOrgRequest req) {
    orgService.create(req.parentId(), req.name(), req.sortOrder(), req.useYn(), req.tenantId());
    return ApiResponse.ok();
  }

  @Operation(summary = "조직 수정")
  @PutMapping("/{orgId}")
  public ApiResponse<Void> update(@PathVariable long orgId, @Valid @RequestBody OrgUpdateCommand cmd) {
    orgService.update(new OrgUpdateCommand(orgId, cmd.parentId(), cmd.name(), cmd.sortOrder(), cmd.useYn()));
    return ApiResponse.ok();
  }

  @Operation(summary = "조직 삭제")
  @DeleteMapping("/{orgId}")
  public ApiResponse<Void> delete(@PathVariable long orgId) {
    orgService.delete(orgId);
    return ApiResponse.ok();
  }
}

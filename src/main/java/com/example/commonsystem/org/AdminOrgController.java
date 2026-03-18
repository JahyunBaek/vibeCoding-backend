package com.example.commonsystem.org;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orgs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrgController {

  private final OrgService orgService;

  public AdminOrgController(OrgService orgService) {
    this.orgService = orgService;
  }

  @GetMapping("/tree")
  public ApiResponse<List<OrgNode>> tree() {
    return ApiResponse.ok(orgService.tree());
  }

  @GetMapping
  public ApiResponse<PageResponse<Org>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(orgService.page(page, size));
  }

  @PostMapping
  public ApiResponse<Void> create(@RequestBody OrgCreateCommand cmd) {
    orgService.create(cmd);
    return ApiResponse.ok();
  }

  @PutMapping("/{orgId}")
  public ApiResponse<Void> update(@PathVariable long orgId, @RequestBody OrgUpdateCommand cmd) {
    orgService.update(new OrgUpdateCommand(orgId, cmd.parentId(), cmd.name(), cmd.sortOrder(), cmd.useYn()));
    return ApiResponse.ok();
  }

  @DeleteMapping("/{orgId}")
  public ApiResponse<Void> delete(@PathVariable long orgId) {
    orgService.delete(orgId);
    return ApiResponse.ok();
  }
}

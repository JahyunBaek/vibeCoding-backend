package com.example.commonsystem.invitation.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.invitation.dto.InvitationDtos.InvitationListRow;
import com.example.commonsystem.invitation.dto.InvitationDtos.InviteRequest;
import com.example.commonsystem.invitation.service.InvitationService;
import com.example.commonsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 초대", description = "사용자 초대 관리")
@RestController
@RequestMapping("/api/admin/invitations")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminInvitationController {

    private final InvitationService invitationService;
    private final TenantContextHolder tenantCtx;

    public AdminInvitationController(InvitationService invitationService,
            TenantContextHolder tenantCtx) {
        this.invitationService = invitationService;
        this.tenantCtx = tenantCtx;
    }

    @Operation(summary = "사용자 초대")
    @PostMapping
    public ApiResponse<String> invite(
            @Valid @RequestBody InviteRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long tenantId = tenantCtx.resolveTenantId(null);
        if (tenantId == null) {
            return ApiResponse.fail("COMMON_400", "테넌트 컨텍스트가 필요합니다.");
        }
        String token = invitationService.invite(tenantId, req.email(), req.roleKey(), principal.getUserId());
        return ApiResponse.ok(token);
    }

    @Operation(summary = "초대 목록 조회")
    @GetMapping
    public ApiResponse<List<InvitationListRow>> list(
            @RequestParam(required = false) Long tenantId) {
        Long resolvedTenantId = tenantCtx.resolveTenantId(tenantId);
        if (resolvedTenantId == null) {
            return ApiResponse.fail("COMMON_400", "테넌트 ID가 필요합니다.");
        }
        return ApiResponse.ok(invitationService.listByTenant(resolvedTenantId));
    }
}

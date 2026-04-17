package com.example.commonsystem.genomics.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.ConsentDtos.ConsentDetail;
import com.example.commonsystem.genomics.dto.ConsentDtos.ConsentListRow;
import com.example.commonsystem.genomics.dto.ConsentDtos.CreateRequest;
import com.example.commonsystem.genomics.dto.ConsentDtos.SignRequest;
import com.example.commonsystem.genomics.service.ConsentService;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Genomic Consents", description = "유전자 검사 동의서 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/consents")
public class ConsentController {

    private final ConsentService consentService;

    @Operation(summary = "동의서 목록")
    @GetMapping
    public ApiResponse<PageResponse<ConsentListRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(consentService.page(page, size, patientId, status));
    }

    @Operation(summary = "동의서 상세")
    @GetMapping("/{consentId}")
    public ApiResponse<ConsentDetail> detail(@PathVariable long consentId) {
        return ApiResponse.ok(consentService.detail(consentId));
    }

    @Operation(summary = "동의서 생성")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateRequest req,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        consentService.create(req, principal.getUserId());
        return ApiResponse.ok();
    }

    @Operation(summary = "동의서 서명")
    @PatchMapping("/{consentId}/sign")
    public ApiResponse<Void> sign(@PathVariable long consentId,
                                  @Valid @RequestBody SignRequest req) {
        consentService.sign(consentId, req);
        return ApiResponse.ok();
    }

    @Operation(summary = "동의서 철회")
    @PatchMapping("/{consentId}/revoke")
    public ApiResponse<Void> revoke(@PathVariable long consentId) {
        consentService.revoke(consentId);
        return ApiResponse.ok();
    }

    @Operation(summary = "동의서 삭제")
    @DeleteMapping("/{consentId}")
    public ApiResponse<Void> delete(@PathVariable long consentId) {
        consentService.delete(consentId);
        return ApiResponse.ok();
    }
}

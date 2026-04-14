package com.example.commonsystem.genomics.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelCreateCommand;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelDetail;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelListRow;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelUpdateCommand;
import com.example.commonsystem.genomics.service.PanelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Gene Panels", description = "유전자 패널 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/panels")
public class PanelController {

    private final PanelService panelService;

    @Operation(summary = "패널 목록 조회")
    @GetMapping
    public ApiResponse<PageResponse<PanelListRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ApiResponse.ok(panelService.page(page, size, search));
    }

    @Operation(summary = "활성 패널 목록 (드롭다운용)")
    @GetMapping("/active")
    public ApiResponse<List<PanelListRow>> active() {
        return ApiResponse.ok(panelService.activePanels());
    }

    @Operation(summary = "패널 상세 조회 (유전자 목록 포함)")
    @GetMapping("/{panelId}")
    public ApiResponse<PanelDetail> detail(@PathVariable long panelId) {
        return ApiResponse.ok(panelService.detail(panelId));
    }

    @Operation(summary = "패널 생성")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody PanelCreateCommand cmd) {
        return ApiResponse.ok(panelService.create(cmd));
    }

    @Operation(summary = "패널 수정")
    @PutMapping("/{panelId}")
    public ApiResponse<Void> update(@PathVariable long panelId,
                                    @Valid @RequestBody PanelUpdateCommand cmd) {
        panelService.update(panelId, cmd);
        return ApiResponse.ok();
    }

    @Operation(summary = "패널 삭제")
    @DeleteMapping("/{panelId}")
    public ApiResponse<Void> delete(@PathVariable long panelId) {
        panelService.delete(panelId);
        return ApiResponse.ok();
    }
}

package com.example.commonsystem.genomics.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleDetail;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleListRow;
import com.example.commonsystem.genomics.dto.SampleDtos.StatusUpdateRequest;
import com.example.commonsystem.genomics.service.SampleService;
import com.example.commonsystem.genomics.service.VcfUploadService;
import com.example.commonsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Genomic Samples", description = "유전체 분석 샘플 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/samples")
public class SampleController {

    private final SampleService sampleService;
    private final VcfUploadService vcfUploadService;

    @Operation(summary = "샘플 목록 조회")
    @GetMapping
    public ApiResponse<PageResponse<SampleListRow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ApiResponse.ok(sampleService.page(page, size, status, search));
    }

    @Operation(summary = "샘플 상세 조회")
    @GetMapping("/{sampleId}")
    public ApiResponse<SampleDetail> detail(@PathVariable long sampleId) {
        return ApiResponse.ok(sampleService.detail(sampleId));
    }

    @Operation(summary = "샘플 등록")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateRequest req,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        long id = sampleService.create(req.patientId(), req.sampleType(),
                req.panelId(), req.note(), principal.getUserId());
        return ApiResponse.ok(id);
    }

    @Operation(summary = "샘플 수정")
    @PutMapping("/{sampleId}")
    public ApiResponse<Void> update(@PathVariable long sampleId,
                                    @Valid @RequestBody UpdateRequest req) {
        sampleService.update(sampleId, req.sampleType(), req.panelId(), req.note());
        return ApiResponse.ok();
    }

    @Operation(summary = "샘플 상태 변경")
    @PatchMapping("/{sampleId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable long sampleId,
                                          @Valid @RequestBody StatusUpdateRequest req) {
        sampleService.updateStatus(sampleId, req.status());
        return ApiResponse.ok();
    }

    @Operation(summary = "VCF 파일 업로드 → 변이 데이터 적재")
    @PostMapping(value = "/{sampleId}/vcf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> uploadVcf(
            @PathVariable long sampleId,
            @RequestPart("file") MultipartFile file) {
        int count = vcfUploadService.uploadAndParse(sampleId, file);
        return ApiResponse.ok(Map.of("sampleId", sampleId, "variantCount", count));
    }

    @Operation(summary = "샘플 삭제")
    @DeleteMapping("/{sampleId}")
    public ApiResponse<Void> delete(@PathVariable long sampleId) {
        sampleService.delete(sampleId);
        return ApiResponse.ok();
    }

    record CreateRequest(
        @NotNull Long patientId,
        @NotBlank String sampleType,
        Long panelId,
        String note
    ) {}

    record UpdateRequest(
        @NotBlank String sampleType,
        Long panelId,
        String note
    ) {}
}

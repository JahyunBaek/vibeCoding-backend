package com.example.commonsystem.genomics.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.genomics.service.DeidentifyService;
import com.example.commonsystem.genomics.service.GenomicAuditService;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "De-identification", description = "비식별화 내보내기")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genomics/export")
public class DeidentifyController {

    private final DeidentifyService deidentifyService;
    private final GenomicAuditService auditService;

    @Operation(summary = "샘플 변이 비식별화 CSV 내보내기")
    @GetMapping("/deidentify/{sampleId}")
    public ResponseEntity<byte[]> exportDeidentified(
            @PathVariable long sampleId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) {

        String csv = deidentifyService.exportDeidentified(sampleId);

        // 감사 로그
        auditService.log(principal.getUserId(), "EXPORT_DATA", "SAMPLE",
                sampleId, "De-identified CSV export", request.getRemoteAddr());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"deidentified_sample_%d.csv\"".formatted(sampleId))
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}

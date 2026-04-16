package com.example.commonsystem.genomics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.domain.Variant;
import com.example.commonsystem.genomics.mapper.SampleMapper;
import com.example.commonsystem.genomics.mapper.VariantMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class VcfUploadService {

    private static final int BATCH_SIZE = 500;

    private final VcfParserService vcfParserService;
    private final VariantMapper variantMapper;
    private final SampleMapper sampleMapper;
    private final TenantContextHolder tenantCtx;

    /**
     * VCF 파일을 파싱하여 해당 샘플의 변이 데이터를 적재한다.
     * 기존 변이가 있으면 삭제 후 재적재한다.
     *
     * @return 적재된 변이 수
     */
    @Transactional
    public int uploadAndParse(long sampleId, MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".vcf") && !filename.endsWith(".vcf.gz"))) {
            throw new RuntimeException("VCF 파일만 업로드 가능합니다 (.vcf, .vcf.gz)");
        }

        Long tenantId = tenantCtx.currentTenantId();

        // 기존 변이 삭제
        variantMapper.deleteBySampleId(sampleId, tenantId);

        // VCF 파싱
        List<Variant> variants;
        try {
            variants = vcfParserService.parse(file.getInputStream(), sampleId, tenantId);
        } catch (Exception e) {
            throw new RuntimeException("VCF 파일 읽기 실패: " + e.getMessage(), e);
        }

        // 배치 INSERT
        for (int i = 0; i < variants.size(); i += BATCH_SIZE) {
            List<Variant> batch = variants.subList(i, Math.min(i + BATCH_SIZE, variants.size()));
            variantMapper.insertBatch(batch);
        }

        // 샘플 상태를 ANALYZING으로 변경 (RECEIVED/EXTRACTED/SEQUENCING 상태일 때)
        sampleMapper.updateStatus(sampleId, "ANALYZING", tenantId);

        log.info("VCF 업로드 완료: sampleId={}, variants={}", sampleId, variants.size());
        return variants.size();
    }
}

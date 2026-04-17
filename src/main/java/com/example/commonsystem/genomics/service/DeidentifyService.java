package com.example.commonsystem.genomics.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantFilter;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;
import com.example.commonsystem.genomics.mapper.VariantMapper;

import lombok.RequiredArgsConstructor;

/**
 * 연구용 비식별화 내보내기 서비스.
 * 환자 식별 정보를 제거하고 임의 ID로 대체하여 변이 데이터를 CSV로 반환한다.
 */
@RequiredArgsConstructor
@Service
public class DeidentifyService {

    private final VariantMapper variantMapper;
    private final TenantContextHolder tenantCtx;

    /**
     * 샘플의 변이 데이터를 비식별화하여 CSV 문자열로 반환한다.
     * - sampleNo → 랜덤 UUID
     * - patientId 제거
     * - 유전체 좌표 및 어노테이션만 포함
     */
    public String exportDeidentified(long sampleId) {
        Long tenantId = tenantCtx.currentTenantId();
        VariantFilter filter = new VariantFilter(sampleId, null, null, null, null, null, null, null);
        List<VariantListRow> variants = variantMapper.findPage(tenantId, filter, 100000, 0);

        String anonymousId = "ANON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        StringBuilder sb = new StringBuilder();
        sb.append("anonymous_id,chromosome,position,ref_allele,alt_allele,variant_type,gene_symbol,")
          .append("hgvs_c,hgvs_p,consequence,impact,acmg_class,zygosity,gnomad_af,clinvar_id\n");

        for (VariantListRow v : variants) {
            sb.append(anonymousId).append(',')
              .append(csvVal(v.chromosome())).append(',')
              .append(v.position()).append(',')
              .append(csvVal(v.refAllele())).append(',')
              .append(csvVal(v.altAllele())).append(',')
              .append(csvVal(v.variantType())).append(',')
              .append(csvVal(v.geneSymbol())).append(',')
              .append(csvVal(v.hgvsC())).append(',')
              .append(csvVal(v.hgvsP())).append(',')
              .append(csvVal(v.consequence())).append(',')
              .append(csvVal(v.impact())).append(',')
              .append(csvVal(v.acmgClass())).append(',')
              .append(csvVal(v.zygosity())).append(',')
              .append(v.gnomadAf() != null ? v.gnomadAf() : "").append(',')
              .append(csvVal(v.clinvarId())).append('\n');
        }

        return sb.toString();
    }

    private String csvVal(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}

package com.example.commonsystem.genomics.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.genomics.dto.StatsDtos.CountItem;

@Mapper
public interface StatsMapper {

    long totalVariants(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId);

    long totalSamples(@Param("tenantId") Long tenantId);

    List<CountItem> countByChromosome(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId);

    List<CountItem> countByVariantType(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId);

    List<CountItem> countByImpact(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId);

    List<CountItem> countByAcmgClass(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId);

    List<CountItem> topGenes(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId,
                             @Param("limit") int limit);
}

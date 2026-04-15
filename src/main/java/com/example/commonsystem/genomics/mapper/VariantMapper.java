package com.example.commonsystem.genomics.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.genomics.domain.Variant;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantDetail;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantFilter;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;

@Mapper
public interface VariantMapper {

    long count(@Param("tenantId") Long tenantId, @Param("filter") VariantFilter filter);

    List<VariantListRow> findPage(@Param("tenantId") Long tenantId,
                                  @Param("filter") VariantFilter filter,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    VariantDetail findById(@Param("variantId") long variantId);

    void delete(@Param("variantId") long variantId);

    void deleteBySampleId(@Param("sampleId") long sampleId);

    void insertBatch(@Param("list") List<Variant> variants);

    int countBySample(@Param("sampleId") long sampleId);
}

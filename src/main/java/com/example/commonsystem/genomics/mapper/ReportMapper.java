package com.example.commonsystem.genomics.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.genomics.dto.ReportDtos.ReportDetail;
import com.example.commonsystem.genomics.dto.ReportDtos.ReportListRow;

@Mapper
public interface ReportMapper {

    long count(@Param("tenantId") Long tenantId, @Param("sampleId") Long sampleId);

    List<ReportListRow> findPage(@Param("tenantId") Long tenantId,
                                 @Param("sampleId") Long sampleId,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    ReportDetail findById(@Param("reportId") long reportId,
                          @Param("tenantId") Long tenantId);

    void insert(@Param("tenantId") Long tenantId,
                @Param("sampleId") long sampleId,
                @Param("title") String title,
                @Param("summary") String summary,
                @Param("variantCount") int variantCount,
                @Param("pathogenicCount") int pathogenicCount,
                @Param("createdBy") Long createdBy);

    void updateStatus(@Param("reportId") long reportId, @Param("status") String status,
                      @Param("tenantId") Long tenantId);

    void delete(@Param("reportId") long reportId,
                @Param("tenantId") Long tenantId);
}

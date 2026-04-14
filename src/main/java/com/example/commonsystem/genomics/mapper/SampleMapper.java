package com.example.commonsystem.genomics.mapper;

import com.example.commonsystem.genomics.dto.SampleDtos.SampleCreateCommand;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleDetail;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleListRow;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleUpdateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SampleMapper {

    long count(@Param("tenantId") Long tenantId,
               @Param("status") String status,
               @Param("search") String search);

    List<SampleListRow> findPage(@Param("tenantId") Long tenantId,
                                 @Param("status") String status,
                                 @Param("search") String search,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    SampleDetail findById(@Param("sampleId") long sampleId);

    void insert(SampleCreateCommand cmd);

    void update(SampleUpdateCommand cmd);

    void updateStatus(@Param("sampleId") long sampleId,
                      @Param("status") String status);

    void delete(@Param("sampleId") long sampleId);

    String generateSampleNo(@Param("tenantId") Long tenantId);
}

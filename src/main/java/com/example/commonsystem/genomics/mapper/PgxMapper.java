package com.example.commonsystem.genomics.mapper;

import com.example.commonsystem.genomics.dto.PgxDtos.PgxListRow;
import com.example.commonsystem.genomics.dto.PgxDtos.PgxMatch;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PgxMapper {

    long count(@Param("tenantId") Long tenantId, @Param("search") String search);

    List<PgxListRow> findPage(@Param("tenantId") Long tenantId,
                              @Param("search") String search,
                              @Param("limit") int limit,
                              @Param("offset") int offset);

    /** 샘플의 변이와 PGx DB를 매칭하여 해당되는 약물유전체 결과를 반환 */
    List<PgxMatch> findMatchesBySample(@Param("tenantId") Long tenantId,
                                       @Param("sampleId") long sampleId);
}

package com.example.commonsystem.genomics.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.genomics.dto.AuditDtos.GenomicAuditRow;

@Mapper
public interface GenomicAuditMapper {

    void insert(@Param("tenantId") Long tenantId,
                @Param("userId") long userId,
                @Param("action") String action,
                @Param("resourceType") String resourceType,
                @Param("resourceId") Long resourceId,
                @Param("detail") String detail,
                @Param("ipAddress") String ipAddress);

    long count(@Param("tenantId") Long tenantId,
               @Param("action") String action,
               @Param("resourceType") String resourceType);

    List<GenomicAuditRow> findPage(@Param("tenantId") Long tenantId,
                                   @Param("action") String action,
                                   @Param("resourceType") String resourceType,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);
}

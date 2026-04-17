package com.example.commonsystem.genomics.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.genomics.dto.ConsentDtos.ConsentDetail;
import com.example.commonsystem.genomics.dto.ConsentDtos.ConsentListRow;

@Mapper
public interface ConsentMapper {

    long count(@Param("tenantId") Long tenantId,
               @Param("patientId") Long patientId,
               @Param("status") String status);

    List<ConsentListRow> findPage(@Param("tenantId") Long tenantId,
                                  @Param("patientId") Long patientId,
                                  @Param("status") String status,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    ConsentDetail findById(@Param("consentId") long consentId,
                           @Param("tenantId") Long tenantId);

    void insert(@Param("tenantId") Long tenantId,
                @Param("patientId") long patientId,
                @Param("sampleId") Long sampleId,
                @Param("consentType") String consentType,
                @Param("expiresAt") LocalDate expiresAt,
                @Param("note") String note,
                @Param("createdBy") Long createdBy);

    void sign(@Param("consentId") long consentId,
              @Param("tenantId") Long tenantId,
              @Param("signedByName") String signedByName,
              @Param("witnessName") String witnessName);

    void revoke(@Param("consentId") long consentId,
                @Param("tenantId") Long tenantId);

    void delete(@Param("consentId") long consentId,
                @Param("tenantId") Long tenantId);
}

package com.example.commonsystem.genomics.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.genomics.dto.PanelDtos.PanelCreateCommand;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelDetail;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelGeneInput;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelListRow;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelUpdateCommand;

@Mapper
public interface PanelMapper {

    long count(@Param("tenantId") Long tenantId, @Param("search") String search);

    List<PanelListRow> findPage(@Param("tenantId") Long tenantId,
                                @Param("search") String search,
                                @Param("limit") int limit,
                                @Param("offset") int offset);

    PanelDetail findById(@Param("panelId") long panelId,
                         @Param("tenantId") Long tenantId);

    List<PanelListRow> findActive(@Param("tenantId") Long tenantId);

    void insert(PanelCreateCommand cmd);

    void update(PanelUpdateCommand cmd);

    void delete(@Param("panelId") long panelId,
                @Param("tenantId") Long tenantId);

    void deleteGenes(@Param("panelId") long panelId);

    void insertGene(@Param("panelId") long panelId,
                    @Param("gene") PanelGeneInput gene);

    void updateGeneCount(@Param("panelId") long panelId);
}

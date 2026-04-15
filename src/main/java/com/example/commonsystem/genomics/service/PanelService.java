package com.example.commonsystem.genomics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelCreateCommand;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelDetail;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelGeneInput;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelListRow;
import com.example.commonsystem.genomics.dto.PanelDtos.PanelUpdateCommand;
import com.example.commonsystem.genomics.mapper.PanelMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PanelService {

    private final PanelMapper panelMapper;
    private final TenantContextHolder tenantCtx;

    public PageResponse<PanelListRow> page(int page, int size, String search) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        String q = (search != null && !search.isBlank()) ? search.trim() : null;

        long total = panelMapper.count(tenantId, q);
        List<PanelListRow> items = panelMapper.findPage(tenantId, q, s, offset);
        return new PageResponse<>(items, p, s, total);
    }

    public PanelDetail detail(long panelId) {
        return panelMapper.findById(panelId);
    }

    public List<PanelListRow> activePanels() {
        return panelMapper.findActive(tenantCtx.currentTenantId());
    }

    @Transactional
    public long create(PanelCreateCommand cmd) {
        cmd.setTenantId(tenantCtx.currentTenantId());
        panelMapper.insert(cmd);
        long panelId = cmd.getPanelId();
        saveGenes(panelId, cmd.getGenes());
        return panelId;
    }

    @Transactional
    public void update(long panelId, PanelUpdateCommand cmd) {
        cmd.setPanelId(panelId);
        panelMapper.update(cmd);
        saveGenes(panelId, cmd.getGenes());
    }

    @Transactional
    public void delete(long panelId) {
        panelMapper.delete(panelId);
    }

    private void saveGenes(long panelId, List<PanelGeneInput> genes) {
        panelMapper.deleteGenes(panelId);
        if (genes != null) {
            for (PanelGeneInput gene : genes) {
                panelMapper.insertGene(panelId, gene);
            }
        }
        panelMapper.updateGeneCount(panelId);
    }
}

package com.example.commonsystem.genomics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleCreateCommand;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleDetail;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleListRow;
import com.example.commonsystem.genomics.dto.SampleDtos.SampleUpdateCommand;
import com.example.commonsystem.genomics.mapper.SampleMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SampleService {

    private final SampleMapper sampleMapper;
    private final TenantContextHolder tenantCtx;

    public PageResponse<SampleListRow> page(int page, int size, String status, String search) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        String st = (status != null && !status.isBlank()) ? status : null;

        long total = sampleMapper.count(tenantId, st, q);
        List<SampleListRow> items = sampleMapper.findPage(tenantId, st, q, s, offset);
        return new PageResponse<>(items, p, s, total);
    }

    public SampleDetail detail(long sampleId) {
        return sampleMapper.findById(sampleId, tenantCtx.currentTenantId());
    }

    @Transactional
    public long create(Long patientId, String sampleType, Long panelId, String note, Long createdBy) {
        Long tenantId = tenantCtx.currentTenantId();
        SampleCreateCommand cmd = new SampleCreateCommand(tenantId, patientId, sampleType, panelId, note, createdBy);
        sampleMapper.insert(cmd);
        return cmd.getSampleId();
    }

    @Transactional
    public void update(long sampleId, String sampleType, Long panelId, String note) {
        Long tenantId = tenantCtx.currentTenantId();
        SampleUpdateCommand cmd = new SampleUpdateCommand();
        cmd.setSampleId(sampleId);
        cmd.setTenantId(tenantId);
        cmd.setSampleType(sampleType);
        cmd.setPanelId(panelId);
        cmd.setNote(note);
        sampleMapper.update(cmd);
    }

    @Transactional
    public void updateStatus(long sampleId, String status) {
        sampleMapper.updateStatus(sampleId, status, tenantCtx.currentTenantId());
    }

    @Transactional
    public void delete(long sampleId) {
        sampleMapper.delete(sampleId, tenantCtx.currentTenantId());
    }
}

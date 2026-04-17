package com.example.commonsystem.genomics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.ConsentDtos.ConsentDetail;
import com.example.commonsystem.genomics.dto.ConsentDtos.ConsentListRow;
import com.example.commonsystem.genomics.dto.ConsentDtos.CreateRequest;
import com.example.commonsystem.genomics.dto.ConsentDtos.SignRequest;
import com.example.commonsystem.genomics.mapper.ConsentMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ConsentService {

    private final ConsentMapper consentMapper;
    private final TenantContextHolder tenantCtx;

    public PageResponse<ConsentListRow> page(int page, int size, Long patientId, String status) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        String st = (status != null && !status.isBlank()) ? status : null;
        long total = consentMapper.count(tenantId, patientId, st);
        List<ConsentListRow> items = consentMapper.findPage(tenantId, patientId, st, s, offset);
        return new PageResponse<>(items, p, s, total);
    }

    public ConsentDetail detail(long consentId) {
        return consentMapper.findById(consentId, tenantCtx.currentTenantId());
    }

    @Transactional
    public void create(CreateRequest req, Long createdBy) {
        consentMapper.insert(tenantCtx.currentTenantId(), req.patientId(),
                req.sampleId(), req.consentType(), req.expiresAt(), req.note(), createdBy);
    }

    @Transactional
    public void sign(long consentId, SignRequest req) {
        consentMapper.sign(consentId, tenantCtx.currentTenantId(),
                req.signedByName(), req.witnessName());
    }

    @Transactional
    public void revoke(long consentId) {
        consentMapper.revoke(consentId, tenantCtx.currentTenantId());
    }

    @Transactional
    public void delete(long consentId) {
        consentMapper.delete(consentId, tenantCtx.currentTenantId());
    }
}

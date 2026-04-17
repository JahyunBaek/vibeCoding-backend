package com.example.commonsystem.genomics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.AuditDtos.GenomicAuditRow;
import com.example.commonsystem.genomics.mapper.GenomicAuditMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GenomicAuditService {

    private final GenomicAuditMapper auditMapper;
    private final TenantContextHolder tenantCtx;

    public void log(long userId, String action, String resourceType,
                    Long resourceId, String detail, String ipAddress) {
        auditMapper.insert(tenantCtx.currentTenantId(), userId,
                action, resourceType, resourceId, detail, ipAddress);
    }

    public PageResponse<GenomicAuditRow> page(int page, int size, String action, String resourceType) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        String a = (action != null && !action.isBlank()) ? action : null;
        String rt = (resourceType != null && !resourceType.isBlank()) ? resourceType : null;
        long total = auditMapper.count(tenantId, a, rt);
        List<GenomicAuditRow> items = auditMapper.findPage(tenantId, a, rt, s, offset);
        return new PageResponse<>(items, p, s, total);
    }
}

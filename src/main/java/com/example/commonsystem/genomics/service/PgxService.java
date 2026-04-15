package com.example.commonsystem.genomics.service;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.PgxDtos.PgxListRow;
import com.example.commonsystem.genomics.dto.PgxDtos.PgxMatch;
import com.example.commonsystem.genomics.mapper.PgxMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PgxService {

    private final PgxMapper pgxMapper;
    private final TenantContextHolder tenantCtx;

    public PageResponse<PgxListRow> page(int page, int size, String search) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        long total = pgxMapper.count(tenantId, q);
        List<PgxListRow> items = pgxMapper.findPage(tenantId, q, s, offset);
        return new PageResponse<>(items, p, s, total);
    }

    /** 샘플 변이 기반 PGx 매칭 */
    public List<PgxMatch> matchBySample(long sampleId) {
        return pgxMapper.findMatchesBySample(tenantCtx.currentTenantId(), sampleId);
    }
}

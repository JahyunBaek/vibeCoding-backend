package com.example.commonsystem.genomics.service;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantDetail;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantFilter;
import com.example.commonsystem.genomics.dto.VariantDtos.VariantListRow;
import com.example.commonsystem.genomics.mapper.VariantMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VariantService {

    private final VariantMapper variantMapper;
    private final TenantContextHolder tenantCtx;

    public PageResponse<VariantListRow> page(int page, int size, VariantFilter filter) {
        Long tenantId = tenantCtx.currentTenantId();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;

        long total = variantMapper.count(tenantId, filter);
        List<VariantListRow> items = variantMapper.findPage(tenantId, filter, s, offset);
        return new PageResponse<>(items, p, s, total);
    }

    public VariantDetail detail(long variantId) {
        return variantMapper.findById(variantId);
    }
}

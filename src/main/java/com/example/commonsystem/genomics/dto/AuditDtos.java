package com.example.commonsystem.genomics.dto;

import java.time.LocalDateTime;

public class AuditDtos {

    public record GenomicAuditRow(
        long auditId,
        long userId,
        String userName,
        String action,
        String resourceType,
        Long resourceId,
        String detail,
        String ipAddress,
        LocalDateTime createdAt
    ) {}
}

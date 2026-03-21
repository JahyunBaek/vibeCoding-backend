package com.example.commonsystem.audit.domain;

public record AuditLog(
    long logId,
    Long tenantId,
    Long userId,
    String username,
    String action,
    String targetType,
    String targetId,
    String detail,
    String ipAddress,
    String createdAt
) {}

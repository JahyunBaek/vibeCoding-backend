package com.example.commonsystem.tenant.dto;

import java.time.Instant;

public record TenantListRow(
    long tenantId,
    String tenantKey,
    String tenantName,
    String planType,
    boolean active,
    long userCount,
    Instant createdAt
) {}

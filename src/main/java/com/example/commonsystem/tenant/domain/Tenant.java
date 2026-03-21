package com.example.commonsystem.tenant.domain;

import java.time.Instant;

public record Tenant(
    long tenantId,
    String tenantKey,
    String tenantName,
    String planType,
    boolean active,
    Instant createdAt
) {}

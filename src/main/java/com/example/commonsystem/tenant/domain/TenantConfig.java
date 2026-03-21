package com.example.commonsystem.tenant.domain;

public record TenantConfig(
    long tenantId,
    String configKey,
    String configValue,
    String updatedAt
) {}

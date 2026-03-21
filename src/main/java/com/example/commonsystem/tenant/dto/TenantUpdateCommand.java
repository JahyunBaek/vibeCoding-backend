package com.example.commonsystem.tenant.dto;

public record TenantUpdateCommand(
    long tenantId,
    String tenantName,
    String planType,
    boolean active
) {}

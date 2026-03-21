package com.example.commonsystem.tenant.dto;

public record TenantCreateResult(
    long tenantId,
    String adminUsername,
    String adminPassword
) {}

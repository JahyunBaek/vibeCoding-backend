package com.example.commonsystem.invitation.domain;

import java.time.Instant;

public record Invitation(
    long invitationId,
    long tenantId,
    String email,
    String roleKey,
    String token,
    String status,
    long invitedBy,
    Instant createdAt,
    Instant expiresAt
) {}

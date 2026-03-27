package com.example.commonsystem.notification.domain;

import java.time.Instant;

public record Notification(
    long notificationId,
    Long tenantId,
    long userId,
    String type,
    String title,
    String message,
    String link,
    boolean readYn,
    Instant createdAt
) {}

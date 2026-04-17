package com.example.commonsystem.notification.dto;

import java.time.LocalDateTime;

public class PreferenceDtos {

    public record PreferenceRow(
        long prefId,
        String channel,
        boolean enabled,
        boolean consented,
        LocalDateTime consentedAt,
        boolean available
    ) {}

    public record UpdateRequest(
        String channel,
        boolean enabled,
        boolean consented
    ) {}
}

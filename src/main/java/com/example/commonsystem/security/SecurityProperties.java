package com.example.commonsystem.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
    String refreshCookieName,
    String refreshCookiePath,
    String refreshCookieSamesite,
    boolean refreshCookieSecure
) {}

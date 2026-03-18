package com.example.commonsystem.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    int accessTokenMinutes,
    int refreshTokenMinutes
) {}

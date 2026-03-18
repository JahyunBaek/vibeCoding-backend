package com.example.commonsystem.common;

public record ApiError(
    String code,
    String message
) {}

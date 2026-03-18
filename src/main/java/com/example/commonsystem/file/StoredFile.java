package com.example.commonsystem.file;

import java.time.Instant;

public record StoredFile(
    long fileId,
    String originalName,
    String savedName,
    String contentType,
    long sizeBytes,
    String storagePath,
    Instant createdAt
) {}

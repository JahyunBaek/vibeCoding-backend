package com.example.commonsystem.board;

import java.time.Instant;

public record Comment(
    long commentId,
    long postId,
    long authorId,
    String authorName,
    String content,
    Instant createdAt
) {}

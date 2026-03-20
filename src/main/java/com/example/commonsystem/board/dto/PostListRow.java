package com.example.commonsystem.board.dto;

import java.time.Instant;

public record PostListRow(
    long postId,
    long boardId,
    String title,
    String authorName,
    Instant createdAt
) {}

package com.example.commonsystem.board;

public record PostUpdateCommand(
    long postId,
    String title,
    String content
) {}

package com.example.commonsystem.board.dto;

public record PostUpdateCommand(
    long postId,
    String title,
    String content
) {}

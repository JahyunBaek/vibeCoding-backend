package com.example.commonsystem.board.dto;

public record CommentCreateCommand(
    long postId,
    long authorId,
    String content
) {}

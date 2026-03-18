package com.example.commonsystem.board;

public record CommentCreateCommand(
    long postId,
    long authorId,
    String content
) {}

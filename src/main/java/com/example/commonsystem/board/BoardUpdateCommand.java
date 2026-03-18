package com.example.commonsystem.board;

public record BoardUpdateCommand(
    long boardId,
    String name,
    String description,
    boolean useYn
) {}

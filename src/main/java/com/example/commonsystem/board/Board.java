package com.example.commonsystem.board;

public record Board(
    long boardId,
    String name,
    String description,
    boolean useYn
) {}

package com.example.commonsystem.board.domain;

public record Board(
    long boardId,
    String name,
    String description,
    boolean useYn
) {}

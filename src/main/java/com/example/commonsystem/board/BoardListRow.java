package com.example.commonsystem.board;

public record BoardListRow(
    long boardId,
    String name,
    String description,
    boolean useYn
) {}

package com.example.commonsystem.board.dto;

public record BoardListRow(
    long boardId,
    String name,
    String description,
    boolean useYn
) {}

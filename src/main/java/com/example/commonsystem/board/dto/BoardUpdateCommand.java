package com.example.commonsystem.board.dto;

public record BoardUpdateCommand(
    long boardId,
    String name,
    String description,
    boolean useYn
) {}

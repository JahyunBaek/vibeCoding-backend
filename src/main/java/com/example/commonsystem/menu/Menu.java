package com.example.commonsystem.menu;

public record Menu(
    long menuId,
    Long parentId,
    String name,
    String path,
    String icon,
    int sortOrder,
    boolean useYn,
    String menuType,
    Long boardId
) {}

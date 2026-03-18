package com.example.commonsystem.menu;

public record MenuUpdateCommand(
    long menuId,
    Long parentId,
    String name,
    String path,
    String icon,
    int sortOrder,
    boolean useYn
) {}

package com.example.commonsystem.menu.dto;

import com.example.commonsystem.menu.domain.Menu;
import java.util.ArrayList;
import java.util.List;

public class MenuNode {
  public long menuId;
  public Long parentId;
  public String name;
  public String path;
  public String icon;
  public int sortOrder;
  public boolean useYn;
  public String menuType;
  public Long boardId;
  public List<MenuNode> children = new ArrayList<>();

  public static MenuNode from(Menu m) {
    MenuNode n = new MenuNode();
    n.menuId = m.menuId();
    n.parentId = m.parentId();
    n.name = m.name();
    n.path = m.path();
    n.icon = m.icon();
    n.sortOrder = m.sortOrder();
    n.useYn = m.useYn();
    n.menuType = m.menuType();
    n.boardId = m.boardId();
    return n;
  }
}

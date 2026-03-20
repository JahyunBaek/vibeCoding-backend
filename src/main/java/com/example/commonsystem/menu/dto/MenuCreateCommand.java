package com.example.commonsystem.menu.dto;

public class MenuCreateCommand {
  private Long parentId;
  private String name;
  private String path;
  private String icon;
  private int sortOrder;
  private boolean useYn;
  private String menuType;
  private Long boardId;

  // filled by MyBatis if useGeneratedKeys works
  private Long menuId;

  public MenuCreateCommand() {}

  public MenuCreateCommand(Long parentId, String name, String path, String icon, int sortOrder, boolean useYn, String menuType, Long boardId) {
    this.parentId = parentId;
    this.name = name;
    this.path = path;
    this.icon = icon;
    this.sortOrder = sortOrder;
    this.useYn = useYn;
    this.menuType = menuType;
    this.boardId = boardId;
  }

  public Long getParentId() { return parentId; }
  public void setParentId(Long parentId) { this.parentId = parentId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }

  public String getIcon() { return icon; }
  public void setIcon(String icon) { this.icon = icon; }

  public int getSortOrder() { return sortOrder; }
  public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

  public boolean isUseYn() { return useYn; }
  public void setUseYn(boolean useYn) { this.useYn = useYn; }

  public String getMenuType() { return menuType; }
  public void setMenuType(String menuType) { this.menuType = menuType; }

  public Long getBoardId() { return boardId; }
  public void setBoardId(Long boardId) { this.boardId = boardId; }

  public Long getMenuId() { return menuId; }
  public void setMenuId(Long menuId) { this.menuId = menuId; }
}

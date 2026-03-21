package com.example.commonsystem.board.dto;

public class BoardCreateCommand {
  private String name;
  private String description;
  private boolean useYn;
  private Long tenantId;
  private Long boardId;

  public BoardCreateCommand() {}

  public BoardCreateCommand(String name, String description, boolean useYn, Long tenantId) {
    this.name = name;
    this.description = description;
    this.useYn = useYn;
    this.tenantId = tenantId;
  }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public boolean isUseYn() { return useYn; }
  public void setUseYn(boolean useYn) { this.useYn = useYn; }

  public Long getTenantId() { return tenantId; }
  public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

  public Long getBoardId() { return boardId; }
  public void setBoardId(Long boardId) { this.boardId = boardId; }
}

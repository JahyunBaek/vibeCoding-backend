package com.example.commonsystem.board;

public class PostCreateCommand {
  private long boardId;
  private String title;
  private String content;
  private long authorId;
  private Long postId;

  public PostCreateCommand() {}

  public PostCreateCommand(long boardId, String title, String content, long authorId) {
    this.boardId = boardId;
    this.title = title;
    this.content = content;
    this.authorId = authorId;
  }

  public long getBoardId() { return boardId; }
  public void setBoardId(long boardId) { this.boardId = boardId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public long getAuthorId() { return authorId; }
  public void setAuthorId(long authorId) { this.authorId = authorId; }

  public Long getPostId() { return postId; }
  public void setPostId(Long postId) { this.postId = postId; }
}

package com.example.commonsystem.board.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PostDetail {
  private long postId;
  private long boardId;
  private String title;
  private String content;
  private long authorId;
  private String authorName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<PostFileItem> files = new ArrayList<>();

  @Getter @Setter
  @AllArgsConstructor
  public static class PostFileItem {
    private long fileId;
    private String originalName;
  }
}

package com.example.commonsystem.board.controller;

import com.example.commonsystem.board.domain.Comment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.board.service.CommentService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글", description = "댓글 CRUD")
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @Operation(summary = "댓글 목록 조회")
  @GetMapping
  public ApiResponse<List<Comment>> list(@PathVariable long postId) {
    return ApiResponse.ok(commentService.list(postId));
  }

  public record CreateCommentRequest(@NotBlank @Size(max = 2000) String content) {}
  public record UpdateCommentRequest(@NotBlank @Size(max = 2000) String content) {}

  @Operation(summary = "댓글 작성")
  @RequiresAction(screen = "BOARD_COMMENT", action = "CREATE")
  @PostMapping
  public ApiResponse<Void> create(@PathVariable long postId,
                                 @AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody CreateCommentRequest req) {
    commentService.create(postId, principal.getUserId(), req.content());
    return ApiResponse.ok();
  }

  @Operation(summary = "댓글 수정")
  @PutMapping("/{commentId}")
  public ApiResponse<Void> update(@PathVariable long postId,
                                  @PathVariable long commentId,
                                  @AuthenticationPrincipal UserPrincipal principal,
                                  @Valid @RequestBody UpdateCommentRequest req) {
    commentService.update(commentId, principal.getUserId(), principal.getRoleKey(), req.content());
    return ApiResponse.ok();
  }

  @Operation(summary = "댓글 삭제")
  @DeleteMapping("/{commentId}")
  public ApiResponse<Void> delete(@PathVariable long postId,
                                  @PathVariable long commentId,
                                  @AuthenticationPrincipal UserPrincipal principal) {
    commentService.delete(commentId, principal.getUserId(), principal.getRoleKey());
    return ApiResponse.ok();
  }
}

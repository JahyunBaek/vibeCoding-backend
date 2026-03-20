package com.example.commonsystem.board.controller;

import com.example.commonsystem.board.domain.Comment;
import com.example.commonsystem.board.service.CommentService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping
  public ApiResponse<List<Comment>> list(@PathVariable long postId) {
    return ApiResponse.ok(commentService.list(postId));
  }

  public record CreateCommentRequest(String content) {}

  @RequiresAction(screen = "BOARD_COMMENT", action = "CREATE")
  @PostMapping
  public ApiResponse<Void> create(@PathVariable long postId,
                                 @AuthenticationPrincipal UserPrincipal principal,
                                 @RequestBody CreateCommentRequest req) {
    commentService.create(postId, principal.getUserId(), req.content());
    return ApiResponse.ok();
  }
}

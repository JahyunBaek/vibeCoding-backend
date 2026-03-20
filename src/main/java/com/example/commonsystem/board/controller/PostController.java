package com.example.commonsystem.board.controller;

import com.example.commonsystem.board.dto.PostDetail;
import com.example.commonsystem.board.dto.PostListRow;
import com.example.commonsystem.board.service.PostService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.IdempotencyService;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/posts")
public class PostController {

  private final PostService postService;
  private final IdempotencyService idempotencyService;

  public PostController(PostService postService, IdempotencyService idempotencyService) {
    this.postService = postService;
    this.idempotencyService = idempotencyService;
  }

  @GetMapping
  public ApiResponse<PageResponse<PostListRow>> list(
      @PathVariable long boardId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search
  ) {
    return ApiResponse.ok(postService.page(boardId, page, size, search));
  }

  @GetMapping("/{postId}")
  public ApiResponse<PostDetail> detail(@PathVariable long postId) {
    return ApiResponse.ok(postService.detail(postId));
  }

  public record CreatePostRequest(String title, String content, List<Long> fileIds) {}

  @RequiresAction(screen = "BOARD_POST", action = "CREATE")
  @PostMapping
  public ApiResponse<Long> create(
      @PathVariable long boardId,
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody CreatePostRequest req
  ) {
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      if (!idempotencyService.tryConsume(idempotencyKey)) {
        throw new AppException(ErrorCode.CONFLICT, "작성 중입니다. 잠시 후 다시 시도해주세요.");
      }
    }

    long postId = postService.create(
        boardId, req.title(), req.content(), principal.getUserId(), req.fileIds());
    return ApiResponse.ok(postId);
  }

  public record UpdatePostRequest(String title, String content, List<Long> fileIds) {}

  @RequiresAction(screen = "BOARD_POST", action = "EDIT")
  @PutMapping("/{postId}")
  public ApiResponse<Void> update(@PathVariable long postId, @RequestBody UpdatePostRequest req) {
    postService.update(postId, req.title(), req.content(), req.fileIds());
    return ApiResponse.ok();
  }

  @RequiresAction(screen = "BOARD_POST", action = "DELETE")
  @DeleteMapping("/{postId}")
  public ApiResponse<Void> delete(@PathVariable long postId) {
    postService.delete(postId);
    return ApiResponse.ok();
  }
}

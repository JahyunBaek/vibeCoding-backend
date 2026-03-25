package com.example.commonsystem.board.controller;

import com.example.commonsystem.board.dto.PostDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.board.dto.PostListRow;
import com.example.commonsystem.board.service.PostService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.IdempotencyService;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글", description = "게시글 CRUD")
@RestController
@RequestMapping("/api/boards/{boardId}/posts")
public class PostController {

  private final PostService postService;
  private final IdempotencyService idempotencyService;

  public PostController(PostService postService, IdempotencyService idempotencyService) {
    this.postService = postService;
    this.idempotencyService = idempotencyService;
  }

  @Operation(summary = "게시글 목록 조회")
  @GetMapping
  public ApiResponse<PageResponse<PostListRow>> list(
      @PathVariable long boardId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search
  ) {
    return ApiResponse.ok(postService.page(boardId, page, size, search));
  }

  @Operation(summary = "게시글 상세 조회")
  @GetMapping("/{postId}")
  public ApiResponse<PostDetail> detail(@PathVariable long postId) {
    return ApiResponse.ok(postService.detail(postId));
  }

  public record CreatePostRequest(
      @NotBlank @Size(max = 200) String title,
      @NotBlank String content,
      List<Long> fileIds
  ) {}

  @Operation(summary = "게시글 작성")
  @RequiresAction(screen = "BOARD_POST", action = "CREATE")
  @PostMapping
  public ApiResponse<Long> create(
      @PathVariable long boardId,
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @Valid @RequestBody CreatePostRequest req
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

  public record UpdatePostRequest(
      @NotBlank @Size(max = 200) String title,
      @NotBlank String content,
      List<Long> fileIds
  ) {}

  @Operation(summary = "게시글 수정")
  @RequiresAction(screen = "BOARD_POST", action = "EDIT")
  @PutMapping("/{postId}")
  public ApiResponse<Void> update(@PathVariable long postId, @Valid @RequestBody UpdatePostRequest req) {
    postService.update(postId, req.title(), req.content(), req.fileIds());
    return ApiResponse.ok();
  }

  @Operation(summary = "게시글 삭제")
  @RequiresAction(screen = "BOARD_POST", action = "DELETE")
  @DeleteMapping("/{postId}")
  public ApiResponse<Void> delete(@PathVariable long postId) {
    postService.delete(postId);
    return ApiResponse.ok();
  }
}

package com.example.commonsystem.board.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.board.dto.BoardListRow;
import com.example.commonsystem.board.service.BoardService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 게시판", description = "게시판 관리")
@RestController
@RequestMapping("/api/admin/boards")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminBoardController {

  private final BoardService boardService;

  public AdminBoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @Operation(summary = "게시판 목록 조회")
  @GetMapping
  public ApiResponse<PageResponse<BoardListRow>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Long tenantId
  ) {
    return ApiResponse.ok(boardService.page(page, size, tenantId));
  }

  public record CreateBoardRequest(
      @NotBlank @Size(max = 100) String name,
      @Size(max = 500) String description,
      Boolean useYn,
      Long tenantId
  ) {}

  @Operation(summary = "게시판 생성")
  @PostMapping
  public ApiResponse<Long> create(@Valid @RequestBody CreateBoardRequest req) {
    long id = boardService.create(req.name(), req.description(), req.useYn() == null || req.useYn(), req.tenantId());
    return ApiResponse.ok(id);
  }

  public record UpdateBoardRequest(
      @NotBlank @Size(max = 100) String name,
      @Size(max = 500) String description,
      Boolean useYn
  ) {}

  @Operation(summary = "게시판 수정")
  @PutMapping("/{boardId}")
  public ApiResponse<Void> update(@PathVariable long boardId, @Valid @RequestBody UpdateBoardRequest req) {
    boardService.update(boardId, req.name(), req.description(), req.useYn() == null || req.useYn());
    return ApiResponse.ok();
  }

  @Operation(summary = "게시판 삭제")
  @DeleteMapping("/{boardId}")
  public ApiResponse<Void> delete(@PathVariable long boardId) {
    boardService.delete(boardId);
    return ApiResponse.ok();
  }
}

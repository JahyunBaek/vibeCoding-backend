package com.example.commonsystem.board.controller;

import com.example.commonsystem.board.dto.BoardListRow;
import com.example.commonsystem.board.service.BoardService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/boards")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminBoardController {

  private final BoardService boardService;

  public AdminBoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @GetMapping
  public ApiResponse<PageResponse<BoardListRow>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Long tenantId
  ) {
    return ApiResponse.ok(boardService.page(page, size, tenantId));
  }

  public record CreateBoardRequest(String name, String description, Boolean useYn, Long tenantId) {}

  @PostMapping
  public ApiResponse<Long> create(@RequestBody CreateBoardRequest req) {
    long id = boardService.create(req.name(), req.description(), req.useYn() == null || req.useYn(), req.tenantId());
    return ApiResponse.ok(id);
  }

  public record UpdateBoardRequest(String name, String description, Boolean useYn) {}

  @PutMapping("/{boardId}")
  public ApiResponse<Void> update(@PathVariable long boardId, @RequestBody UpdateBoardRequest req) {
    boardService.update(boardId, req.name(), req.description(), req.useYn() == null || req.useYn());
    return ApiResponse.ok();
  }

  @DeleteMapping("/{boardId}")
  public ApiResponse<Void> delete(@PathVariable long boardId) {
    boardService.delete(boardId);
    return ApiResponse.ok();
  }
}

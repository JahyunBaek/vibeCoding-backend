package com.example.commonsystem.board;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/boards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBoardController {

  private final BoardService boardService;

  public AdminBoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @GetMapping
  public ApiResponse<PageResponse<BoardListRow>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.ok(boardService.page(page, size));
  }

  public record CreateBoardRequest(String name, String description, Boolean useYn) {}

  @PostMapping
  public ApiResponse<Long> create(@RequestBody CreateBoardRequest req) {
    long id = boardService.create(req.name(), req.description(), req.useYn() == null || req.useYn());
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

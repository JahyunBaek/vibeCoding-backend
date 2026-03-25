package com.example.commonsystem.board.controller;

import com.example.commonsystem.board.domain.Board;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.board.service.BoardService;
import com.example.commonsystem.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시판", description = "게시판 조회")
@RestController
@RequestMapping("/api/boards")
public class BoardController {

  private final BoardService boardService;

  public BoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @Operation(summary = "활성 게시판 목록 조회")
  @GetMapping
  public ApiResponse<List<Board>> activeBoards() {
    return ApiResponse.ok(boardService.activeBoards());
  }
}

package com.example.commonsystem.board.service;

import com.example.commonsystem.board.dto.BoardCreateCommand;
import com.example.commonsystem.board.dto.BoardListRow;
import com.example.commonsystem.board.dto.BoardUpdateCommand;
import com.example.commonsystem.board.domain.Board;
import com.example.commonsystem.board.mapper.BoardMapper;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.menu.domain.Menu;
import com.example.commonsystem.menu.dto.MenuCreateCommand;
import com.example.commonsystem.menu.dto.MenuUpdateCommand;
import com.example.commonsystem.menu.mapper.MenuMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

  private final BoardMapper boardMapper;
  private final MenuMapper menuMapper;

  public BoardService(BoardMapper boardMapper, MenuMapper menuMapper) {
    this.boardMapper = boardMapper;
    this.menuMapper = menuMapper;
  }

  public List<Board> activeBoards() {
    return boardMapper.findActive();
  }

  public PageResponse<BoardListRow> page(int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = boardMapper.count();
    List<BoardListRow> items = boardMapper.findPage(s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  @Transactional
  public long create(String name, String description, boolean useYn) {
    BoardCreateCommand cmd = new BoardCreateCommand(name, description, useYn);
    boardMapper.insert(cmd);
    long boardId = cmd.getBoardId();

    Long boardsRoot = menuMapper.findBoardsRootMenuId();
    if (boardsRoot == null) boardsRoot = 2L;

    long maxSort = menuMapper.findMaxSortOrder(boardsRoot);
    int sortOrder = (int) (maxSort + 10);

    MenuCreateCommand menuCmd = new MenuCreateCommand(
        boardsRoot,
        name,
        "/boards/" + boardId,
        "clipboard-list",
        sortOrder,
        useYn,
        "BOARD",
        boardId
    );
    menuMapper.insert(menuCmd);
    long menuId = menuCmd.getMenuId();

    // 기본: ADMIN/USER 둘 다 접근 가능
    menuMapper.insertRole(menuId, "ADMIN");
    menuMapper.insertRole(menuId, "USER");

    return boardId;
  }

  @Transactional
  public void update(long boardId, String name, String description, boolean useYn) {
    boardMapper.update(new BoardUpdateCommand(boardId, name, description, useYn));

    Menu menu = menuMapper.findByBoardId(boardId);
    if (menu != null) {
      menuMapper.update(new MenuUpdateCommand(
          menu.menuId(),
          menu.parentId(),
          name,
          menu.path(),
          menu.icon(),
          menu.sortOrder(),
          useYn
      ));
    }
  }

  @Transactional
  public void delete(long boardId) {
    Menu menu = menuMapper.findByBoardId(boardId);
    if (menu != null) {
      menuMapper.delete(menu.menuId());
    }
    boardMapper.delete(boardId);
  }
}

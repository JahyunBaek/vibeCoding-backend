package com.example.commonsystem.board.service;

import com.example.commonsystem.board.domain.Board;
import com.example.commonsystem.board.dto.BoardCreateCommand;
import com.example.commonsystem.board.dto.BoardListRow;
import com.example.commonsystem.board.dto.BoardUpdateCommand;
import com.example.commonsystem.board.mapper.BoardMapper;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
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
  private final TenantContextHolder tenantCtx;

  public BoardService(BoardMapper boardMapper, MenuMapper menuMapper, TenantContextHolder tenantCtx) {
    this.boardMapper = boardMapper;
    this.menuMapper  = menuMapper;
    this.tenantCtx   = tenantCtx;
  }

  public List<Board> activeBoards() {
    return boardMapper.findActive(tenantCtx.currentTenantId());
  }

  public PageResponse<BoardListRow> page(int page, int size, Long tenantIdOverride) {
    Long tenantId = tenantCtx.resolveTenantId(tenantIdOverride);
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = boardMapper.count(tenantId);
    List<BoardListRow> items = boardMapper.findPage(tenantId, s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  @Transactional
  public long create(String name, String description, boolean useYn, Long tenantIdOverride) {
    Long tenantId = tenantCtx.resolveTenantId(tenantIdOverride);
    BoardCreateCommand cmd = new BoardCreateCommand(name, description, useYn, tenantId);
    boardMapper.insert(cmd);
    long boardId = cmd.getBoardId();

    Long boardsRoot = menuMapper.findBoardsRootMenuId(tenantId);
    if (boardsRoot == null) boardsRoot = 2L; // fallback

    long maxSort = menuMapper.findMaxSortOrder(boardsRoot);
    int sortOrder = (int) (maxSort + 10);

    MenuCreateCommand menuCmd = new MenuCreateCommand(
        boardsRoot, name, "/boards/" + boardId, "clipboard-list",
        sortOrder, useYn, "BOARD", boardId, tenantId
    );
    menuMapper.insert(menuCmd);
    long menuId = menuCmd.getMenuId();

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
          menu.menuId(), menu.parentId(), name, menu.path(), menu.icon(), menu.sortOrder(), useYn
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

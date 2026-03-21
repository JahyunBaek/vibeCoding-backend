package com.example.commonsystem.board.mapper;

import com.example.commonsystem.board.domain.Board;
import com.example.commonsystem.board.dto.BoardCreateCommand;
import com.example.commonsystem.board.dto.BoardListRow;
import com.example.commonsystem.board.dto.BoardUpdateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
  List<Board> findActive(@Param("tenantId") Long tenantId);

  long count(@Param("tenantId") Long tenantId);
  List<BoardListRow> findPage(@Param("tenantId") Long tenantId,
      @Param("limit") int limit, @Param("offset") int offset);

  Board findById(@Param("boardId") long boardId);

  void insert(BoardCreateCommand cmd);
  void update(BoardUpdateCommand cmd);
  void delete(@Param("boardId") long boardId);
}

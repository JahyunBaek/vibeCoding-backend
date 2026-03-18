package com.example.commonsystem.board;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
  List<Board> findActive();

  long count();
  List<BoardListRow> findPage(@Param("limit") int limit, @Param("offset") int offset);

  Board findById(@Param("boardId") long boardId);

  void insert(BoardCreateCommand cmd);
  void update(BoardUpdateCommand cmd);
  void delete(@Param("boardId") long boardId);
}

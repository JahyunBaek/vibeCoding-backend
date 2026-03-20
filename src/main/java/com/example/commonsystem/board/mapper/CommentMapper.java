package com.example.commonsystem.board.mapper;

import com.example.commonsystem.board.domain.Comment;
import com.example.commonsystem.board.dto.CommentCreateCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  List<Comment> findByPost(@Param("postId") long postId);
  void insert(CommentCreateCommand cmd);
}

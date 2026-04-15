package com.example.commonsystem.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.board.domain.Comment;
import com.example.commonsystem.board.dto.CommentCreateCommand;

@Mapper
public interface CommentMapper {
  List<Comment> findByPost(@Param("postId") long postId);
  Comment findById(@Param("commentId") long commentId);
  void insert(CommentCreateCommand cmd);
  void update(@Param("commentId") long commentId, @Param("content") String content);
  void delete(@Param("commentId") long commentId);
}

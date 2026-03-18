package com.example.commonsystem.board;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
  List<Comment> findByPost(@Param("postId") long postId);
  void insert(CommentCreateCommand cmd);
}

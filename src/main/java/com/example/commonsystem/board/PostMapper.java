package com.example.commonsystem.board;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {
  long countByBoard(@Param("boardId") long boardId);
  List<PostListRow> findPageByBoard(@Param("boardId") long boardId, @Param("limit") int limit, @Param("offset") int offset);

  PostDetail findDetail(@Param("postId") long postId);

  void insert(PostCreateCommand cmd);
  void update(PostUpdateCommand cmd);
  void delete(@Param("postId") long postId);

  void deleteFiles(@Param("postId") long postId);
  void insertFile(@Param("postId") long postId, @Param("fileId") long fileId);
}

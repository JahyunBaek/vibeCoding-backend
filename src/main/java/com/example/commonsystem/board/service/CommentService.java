package com.example.commonsystem.board.service;

import com.example.commonsystem.board.domain.Comment;
import com.example.commonsystem.board.dto.CommentCreateCommand;
import com.example.commonsystem.board.mapper.CommentMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

  private final CommentMapper commentMapper;

  public CommentService(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  public List<Comment> list(long postId) {
    return commentMapper.findByPost(postId);
  }

  @Transactional
  public void create(long postId, long authorId, String content) {
    commentMapper.insert(new CommentCreateCommand(postId, authorId, content));
  }
}

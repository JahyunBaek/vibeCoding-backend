package com.example.commonsystem.board.service;

import com.example.commonsystem.board.domain.Comment;
import com.example.commonsystem.board.dto.CommentCreateCommand;
import com.example.commonsystem.board.mapper.CommentMapper;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
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

  @Transactional
  public void update(long commentId, long userId, String roleKey, String content) {
    Comment comment = commentMapper.findById(commentId);
    if (comment == null) throw new AppException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다.");
    if (comment.authorId() != userId && !"ADMIN".equals(roleKey) && !"SUPER_ADMIN".equals(roleKey)) {
      throw new AppException(ErrorCode.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다.");
    }
    commentMapper.update(commentId, content);
  }

  @Transactional
  public void delete(long commentId, long userId, String roleKey) {
    Comment comment = commentMapper.findById(commentId);
    if (comment == null) throw new AppException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다.");
    if (comment.authorId() != userId && !"ADMIN".equals(roleKey) && !"SUPER_ADMIN".equals(roleKey)) {
      throw new AppException(ErrorCode.FORBIDDEN, "본인의 댓글만 삭제할 수 있습니다.");
    }
    commentMapper.delete(commentId);
  }
}

package com.example.commonsystem.board;

import com.example.commonsystem.common.PageResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

  private final PostMapper postMapper;

  public PostService(PostMapper postMapper) {
    this.postMapper = postMapper;
  }

  public PageResponse<PostListRow> page(long boardId, int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = postMapper.countByBoard(boardId);
    List<PostListRow> items = postMapper.findPageByBoard(boardId, s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  public PostDetail detail(long postId) {
    return postMapper.findDetail(postId);
  }

  @Transactional
  public long create(long boardId, String title, String content, long authorId, List<Long> fileIds) {
    PostCreateCommand cmd = new PostCreateCommand(boardId, title, content, authorId);
    postMapper.insert(cmd);
    long postId = cmd.getPostId();

    setFiles(postId, fileIds);
    return postId;
  }

  @Transactional
  public void update(long postId, String title, String content, List<Long> fileIds) {
    postMapper.update(new PostUpdateCommand(postId, title, content));
    setFiles(postId, fileIds);
  }

  @Transactional
  public void delete(long postId) {
    postMapper.delete(postId);
  }

  private void setFiles(long postId, List<Long> fileIds) {
    postMapper.deleteFiles(postId);
    if (fileIds != null) {
      for (Long fid : fileIds) {
        if (fid != null) postMapper.insertFile(postId, fid);
      }
    }
  }
}

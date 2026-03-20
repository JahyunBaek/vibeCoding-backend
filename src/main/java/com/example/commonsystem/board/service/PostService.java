package com.example.commonsystem.board.service;

import com.example.commonsystem.board.dto.PostCreateCommand;
import com.example.commonsystem.board.dto.PostDetail;
import com.example.commonsystem.board.dto.PostListRow;
import com.example.commonsystem.board.dto.PostUpdateCommand;
import com.example.commonsystem.board.mapper.PostMapper;
import com.example.commonsystem.common.PageResponse;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

  private final PostMapper postMapper;

  @Value("${app.base-url:http://localhost:8888}")
  private String baseUrl;

  public PostService(PostMapper postMapper) {
    this.postMapper = postMapper;
  }

  public PageResponse<PostListRow> page(long boardId, int page, int size, String search) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    String q = (search != null && !search.isBlank()) ? search.trim() : null;
    long total = postMapper.countByBoard(boardId, q);
    List<PostListRow> items = postMapper.findPageByBoard(boardId, s, offset, q);
    return new PageResponse<>(items, p, s, total);
  }

  public PostDetail detail(long postId) {
    return postMapper.findDetail(postId);
  }

  @Transactional
  public long create(long boardId, String title, String content, long authorId, List<Long> fileIds) {
    String sanitized = sanitize(content);
    PostCreateCommand cmd = new PostCreateCommand(boardId, title, sanitized, authorId);
    postMapper.insert(cmd);
    long postId = cmd.getPostId();
    setFiles(postId, fileIds);
    return postId;
  }

  @Transactional
  public void update(long postId, String title, String content, List<Long> fileIds) {
    String sanitized = sanitize(content);
    postMapper.update(new PostUpdateCommand(postId, title, sanitized));
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

  /**
   * jsoup XSS 방어.
   * 인라인 이미지는 /images/board/... 상대 경로로 저장되므로
   * baseUrl 기준으로 절대화 → 프로토콜 검증 → 다시 상대 경로로 복원한다.
   */
  private String sanitize(String rawHtml) {
    if (rawHtml == null || rawHtml.isBlank()) return "";

    Safelist safelist = Safelist.relaxed()
        .addTags("u", "s", "strike", "pre", "code", "hr", "span", "div")
        .addAttributes(":all", "class", "style")
        .addAttributes("img", "width", "height", "style")
        .addProtocols("img", "src", "http", "https");

    String clean = Jsoup.clean(
        rawHtml,
        baseUrl,
        safelist,
        new Document.OutputSettings().prettyPrint(false)
    );

    // jsoup이 /images/... 를 {baseUrl}/images/... 로 절대화한 것을 다시 상대 경로로 복원
    if (!baseUrl.isBlank()) {
      clean = clean.replace("src=\"" + baseUrl + "/images/", "src=\"/images/");
    }

    return clean;
  }
}

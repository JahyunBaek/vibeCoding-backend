package com.example.commonsystem.file.controller;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.file.domain.StoredFile;
import com.example.commonsystem.file.service.FileService;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  public record UploadResponse(long fileId, String originalName, long sizeBytes) {}

  // ─── 일반 첨부파일 ────────────────────────────────────────────────

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<UploadResponse> upload(@RequestPart("file") MultipartFile file) {
    StoredFile saved = fileService.save(file);
    return ApiResponse.ok(new UploadResponse(saved.fileId(), saved.originalName(), saved.sizeBytes()));
  }

  @DeleteMapping("/{fileId}")
  public ApiResponse<Void> delete(@PathVariable long fileId) {
    fileService.delete(fileId);
    return ApiResponse.ok(null);
  }

  @GetMapping("/{fileId}/download")
  public ResponseEntity<Resource> download(@PathVariable long fileId) {
    StoredFile f = fileService.get(fileId);
    Path p = fileService.resolvePath(f);
    String ct = f.contentType() == null ? "application/octet-stream" : f.contentType();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + f.originalName().replaceAll("\"", "") + "\"")
        .contentType(MediaType.parseMediaType(ct))
        .body(new FileSystemResource(p));
  }

  // ─── 에디터 인라인 이미지 ─────────────────────────────────────────

  public record InlineImageResponse(String url) {}

  /**
   * 에디터 Ctrl+V / 이미지 삽입 시 호출.
   * images/board/{year}/{month}/{day}/ 에 바로 저장하고 정적 URL을 반환한다.
   * post_files 에 등록되지 않으므로 첨부파일 목록에 나타나지 않는다.
   */
  @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<InlineImageResponse> uploadInlineImage(@RequestPart("file") MultipartFile file) {
    String url = fileService.saveInlineImage(file);
    return ApiResponse.ok(new InlineImageResponse(url));
  }
}

package com.example.commonsystem.file;

import com.example.commonsystem.common.ApiResponse;
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

    FileSystemResource res = new FileSystemResource(p);
    String ct = f.contentType() == null ? "application/octet-stream" : f.contentType();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f.originalName().replaceAll("\"", "") + "\"")
        .contentType(MediaType.parseMediaType(ct))
        .body(res);
  }
}

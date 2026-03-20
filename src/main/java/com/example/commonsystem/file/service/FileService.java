package com.example.commonsystem.file.service;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.file.domain.StoredFile;
import com.example.commonsystem.file.dto.FileCreateCommand;
import com.example.commonsystem.file.mapper.FileMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  private final FileMapper fileMapper;

  @Value("${app.file-storage-path:./storage}")
  private String baseDir;

  public FileService(FileMapper fileMapper) {
    this.fileMapper = fileMapper;
  }

  // ─── 일반 첨부파일 저장 ──────────────────────────────────────────

  @Transactional
  public StoredFile save(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new AppException(ErrorCode.VALIDATION, "Empty file");
    }

    String original = file.getOriginalFilename();
    if (original == null) original = "file";

    LocalDate d = LocalDate.now();
    String relDir = String.format("%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());

    String safeOriginal = original.replaceAll("[\\/]+", "_");
    String saved = UUID.randomUUID() + "_" + safeOriginal;

    Path dir = Path.of(baseDir, relDir);
    Path full = dir.resolve(saved);

    try {
      Files.createDirectories(dir);
      file.transferTo(full);
    } catch (IOException e) {
      throw new AppException(ErrorCode.INTERNAL, "Failed to save file");
    }

    FileCreateCommand cmd = new FileCreateCommand(
        original, saved, file.getContentType(), file.getSize(), dir.toString()
    );
    fileMapper.insert(cmd);

    return fileMapper.findById(cmd.getFileId());
  }

  public StoredFile get(long fileId) {
    StoredFile f = fileMapper.findById(fileId);
    if (f == null) throw new AppException(ErrorCode.NOT_FOUND, "File not found");
    return f;
  }

  public Path resolvePath(StoredFile f) {
    return Path.of(f.storagePath(), f.savedName());
  }

  @Transactional
  public void delete(long fileId) {
    StoredFile f = fileMapper.findById(fileId);
    if (f == null) return;
    try { Files.deleteIfExists(resolvePath(f)); } catch (IOException ignored) {}
    fileMapper.deleteById(fileId);
  }

  // ─── 에디터 인라인 이미지 저장 (영구 저장, post_files 미등록) ──────

  /**
   * 에디터 붙여넣기/삽입 이미지를 images/board/{year}/{month}/{day}/ 에 바로 저장한다.
   * 파일 레코드는 files 테이블에만 남기고 post_files 에는 연결하지 않는다.
   *
   * @return 정적 서빙 경로 예) /images/board/2026/03/20/uuid.jpg
   */
  @Transactional
  public String saveInlineImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new AppException(ErrorCode.VALIDATION, "Empty file");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new AppException(ErrorCode.VALIDATION, "이미지 파일만 업로드할 수 있습니다.");
    }

    String original = file.getOriginalFilename();
    if (original == null) original = "image";

    String ext = resolveExt(original, contentType);
    String savedName = UUID.randomUUID() + "." + ext;

    LocalDate d = LocalDate.now();
    String relDir = String.format("images/board/%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
    Path dir = Path.of(baseDir, relDir);
    Path full = dir.resolve(savedName);

    try {
      Files.createDirectories(dir);
      file.transferTo(full);
    } catch (IOException e) {
      throw new AppException(ErrorCode.INTERNAL, "이미지 저장 실패");
    }

    long sizeBytes = 0;
    try { sizeBytes = Files.size(full); } catch (IOException ignored) {}

    FileCreateCommand cmd = new FileCreateCommand(original, savedName, contentType, sizeBytes, dir.toString());
    fileMapper.insert(cmd);

    return "/" + relDir + "/" + savedName;
  }

  // ─── 헬퍼 ────────────────────────────────────────────────────────

  private String resolveExt(String originalFilename, String contentType) {
    if (originalFilename != null && originalFilename.contains(".")) {
      String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
      if (!ext.isBlank()) return ext;
    }
    return switch (contentType) {
      case "image/jpeg"   -> "jpg";
      case "image/png"    -> "png";
      case "image/gif"    -> "gif";
      case "image/webp"   -> "webp";
      case "image/svg+xml"-> "svg";
      default             -> "bin";
    };
  }

  private String resolveContentType(String ext) {
    return switch (ext.toLowerCase()) {
      case "jpg", "jpeg" -> "image/jpeg";
      case "png"         -> "image/png";
      case "gif"         -> "image/gif";
      case "webp"        -> "image/webp";
      case "svg"         -> "image/svg+xml";
      default            -> "application/octet-stream";
    };
  }
}

package com.example.commonsystem.file.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.file.domain.StoredFile;
import com.example.commonsystem.file.dto.FileCreateCommand;
import com.example.commonsystem.file.mapper.FileMapper;
import com.example.commonsystem.file.storage.FileStorageProvider;

@Service
public class FileService {

  private final FileMapper fileMapper;
  private final TenantContextHolder tenantCtx;
  private final FileStorageProvider storage;

  public FileService(FileMapper fileMapper, TenantContextHolder tenantCtx,
      FileStorageProvider storage) {
    this.fileMapper = fileMapper;
    this.tenantCtx = tenantCtx;
    this.storage = storage;
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
    String savedName = UUID.randomUUID() + "_" + safeOriginal;
    String key = relDir + "/" + savedName;

    try {
      storage.store(key, file.getInputStream(), file.getSize(), file.getContentType());
    } catch (IOException e) {
      throw new AppException(ErrorCode.INTERNAL, "Failed to save file");
    }

    FileCreateCommand cmd = new FileCreateCommand(
        original, savedName, file.getContentType(), file.getSize(), relDir, tenantCtx.currentTenantId()
    );
    fileMapper.insert(cmd);

    return fileMapper.findById(cmd.getFileId());
  }

  public StoredFile get(long fileId) {
    StoredFile f = fileMapper.findById(fileId);
    if (f == null) throw new AppException(ErrorCode.NOT_FOUND, "File not found");
    return f;
  }

  /**
   * 저장소 키를 조합한다. storagePath + "/" + savedName
   */
  public String resolveKey(StoredFile f) {
    return f.storagePath() + "/" + f.savedName();
  }

  /**
   * 파일을 Resource로 반환한다 (다운로드용).
   */
  public Resource loadAsResource(StoredFile f) {
    return storage.load(resolveKey(f));
  }

  @Transactional
  public void delete(long fileId) {
    StoredFile f = fileMapper.findById(fileId);
    if (f == null) return;
    storage.delete(resolveKey(f));
    fileMapper.deleteById(fileId);
  }

  // ─── 에디터 인라인 이미지 저장 (영구 저장, post_files 미등록) ──────

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
    String key = relDir + "/" + savedName;

    try {
      storage.store(key, file.getInputStream(), file.getSize(), contentType);
    } catch (IOException e) {
      throw new AppException(ErrorCode.INTERNAL, "이미지 저장 실패");
    }

    FileCreateCommand cmd = new FileCreateCommand(
        original, savedName, contentType, file.getSize(), relDir, tenantCtx.currentTenantId()
    );
    fileMapper.insert(cmd);

    return storage.resolvePublicUrl(key);
  }

  /**
   * 저장소에서 파일을 삭제한다 (OrphanFileCleanupJob에서 사용).
   */
  public void deletePhysical(StoredFile f) {
    storage.delete(resolveKey(f));
  }

  // ─── 헬퍼 ────────────────────────────────────────────────────────

  private String resolveExt(String originalFilename, String contentType) {
    if (originalFilename != null && originalFilename.contains(".")) {
      String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
      if (!ext.isBlank()) return ext;
    }
    return switch (contentType) {
      case "image/jpeg"    -> "jpg";
      case "image/png"     -> "png";
      case "image/gif"     -> "gif";
      case "image/webp"    -> "webp";
      case "image/svg+xml" -> "svg";
      default              -> "bin";
    };
  }
}

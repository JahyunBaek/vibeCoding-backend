package com.example.commonsystem.file;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
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
        original,
        saved,
        file.getContentType(),
        file.getSize(),
        dir.toString()
    );
    fileMapper.insert(cmd);
    long fileId = cmd.getFileId();

    return fileMapper.findById(fileId);
  }

  public StoredFile get(long fileId) {
    StoredFile f = fileMapper.findById(fileId);
    if (f == null) {
      throw new AppException(ErrorCode.NOT_FOUND, "File not found");
    }
    return f;
  }

  public Path resolvePath(StoredFile f) {
    return Path.of(f.storagePath(), f.savedName());
  }

  @Transactional
  public void delete(long fileId) {
    StoredFile f = fileMapper.findById(fileId);
    if (f == null) return; // 이미 삭제됐거나 존재하지 않음

    // 파일시스템에서 삭제 (실패해도 DB는 정리)
    try {
      Files.deleteIfExists(resolvePath(f));
    } catch (IOException ignored) {
    }

    fileMapper.deleteById(fileId);
  }
}

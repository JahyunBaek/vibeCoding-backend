package com.example.commonsystem.file.service;

import com.example.commonsystem.file.domain.StoredFile;
import com.example.commonsystem.file.mapper.FileMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrphanFileCleanupJob {

  private final FileMapper fileMapper;

  @Value("${app.file-storage-path:./storage}")
  private String baseDir;

  public OrphanFileCleanupJob(FileMapper fileMapper) {
    this.fileMapper = fileMapper;
  }

  @Scheduled(cron = "0 0 3 * * *")
  public void cleanupOrphanFiles() {
    log.info("고아 파일 정리 시작");
    int deleted = 0;

    List<StoredFile> orphans = fileMapper.findOrphans(24, 500);
    for (StoredFile f : orphans) {
      try {
        Path filePath = Path.of(f.storagePath(), f.savedName());
        Files.deleteIfExists(filePath);
        fileMapper.deleteById(f.fileId());
        deleted++;
      } catch (IOException e) {
        log.warn("파일 삭제 실패: fileId={}, path={}", f.fileId(), f.storagePath(), e);
      }
    }

    log.info("고아 파일 정리 완료: {}건 삭제", deleted);
  }
}
